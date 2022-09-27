package server;

import commonResources.Email;
import javafx.application.Platform;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.ObservableMap;

import java.io.*;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;

public class ServerEmailModel {

    private final ObservableMap<String, ArrayList<Email>> serverMailMap = FXCollections.observableHashMap();
    private final ObservableList<Log> logList = FXCollections.observableArrayList();
    private final ObservableList<String> usersList = FXCollections.observableArrayList();
    private final ObjectProperty<Log> currentLog = new SimpleObjectProperty<>(null);
    private final ObjectProperty<Email> currentEmail = new SimpleObjectProperty<>(null);
    private final ObjectProperty<String> currentUser = new SimpleObjectProperty<>(null);

    public ServerEmailModel() {
        String csvUsers = "src/commonResources/users_list.txt";
        String line = "";
        BufferedReader br;
        try {
            br = new BufferedReader(new FileReader(csvUsers));
            while ((line = br.readLine()) != null) {
                usersList.add(line);
            }
            br.close();
            line = "";
        } catch (IOException e) {
            e.printStackTrace();
        }

        for(String user : usersList) {
            try {
                String csvEmails = "src/server/" + user + ".txt";
                File file = new File(csvEmails);
                file.createNewFile();
                ArrayList<Email> mailList = new ArrayList<>();
                br = new BufferedReader(new FileReader(csvEmails));
                while ((line = br.readLine()) != null) {
                    String[] email = line.split("#");
                    int id =  Integer.parseInt(email[0]);
                    boolean read = Boolean.parseBoolean(email[7]);
                    Email mail = new Email(id, email[1], email[2], email[3], email[4], email[5], email[6], read);
                    if(mail.getReceiver().equals(user)) {
                        if (user.equals(email[2]))
                            mailList.add(0, mail);
                    }
                }
                this.serverMailMap.put(user, mailList);
                br.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public ObservableMap<String, ArrayList<Email>> getServerMailMap() {
        return serverMailMap;
    }

    public ObservableList<Log> getLogList() {
        return logList;
    }

    public ObservableList<String> getUsersList() { return usersList; }

    public ObservableList<Email> getServerMailList() {
        ObservableList<Email> mailList = FXCollections.observableArrayList();
        for (String address : serverMailMap.keySet()) {
            mailList.addAll(serverMailMap.get(address));
        }
        return mailList;
    }

    public ObjectProperty<Log> currentLogProperty() {
        return currentLog;
    }

    public final Log getCurrentLog() {
        return currentLog.get();
    }

    public final void setCurrentLog(Log log) {
        currentLog.set(log);
    }

    public ObjectProperty<Email> currentEmailProperty() {
        return currentEmail;
    }

    public final Email getCurrentEmail() {
        return currentEmail.get();
    }

    public final void setCurrentEmail(Email email) {
        currentEmail.set(email);
    }

    public ObjectProperty<String> currentUserProperty() {
        return currentUser;
    }

    public final String getCurrentUser() {
        return currentUser.get();
    }

    public final void setCurrentUser(String user) {
        currentUser.set(user);
    }

    public void addLog(String logText) {
        DateFormat dateFormat = new SimpleDateFormat("HH:mm:ss");
        Date date = new Date();
        logList.add(new Log(logText, dateFormat.format(date)));
    }

    public void setReadEmail(String address, Email mail) {
        findEmail(mail, address).setRead(true);
    }

    public void sendMail(Email mail, String receiver) {
        if (serverMailMap.containsKey(receiver)) {
                mail.setText(mail.getText());
                serverMailMap.get(receiver).add(0, mail);
                serverMailMap.get(receiver).get(0).setId(Integer.parseInt(new StringBuilder()
                    .append(getUsersList().indexOf(receiver)+1)
                    .append(getAddressMailList(receiver).size()).toString()));
                Platform.runLater(() -> {
                    addLog(mail.getSender() + " has sent a new email to: " + receiver);
                });
        } else {
            Platform.runLater(() -> {
                addLog("Error sending email " + mail.getSender() + " to " + receiver + ": Email address does not exist");
            });
        }
    }

    public ArrayList<Email> getAddressMailList(String address) {
        return serverMailMap.get(address);
    }

    public void deleteEmail(String address, Email mail) {
        serverMailMap.get(address).remove(mail);
        Platform.runLater(() -> {
            addLog("Email " + mail + " deleted from account: " + address);
        });
    }

    public Email findEmail(Email email, String user) {
        for(Email emailToFind : getAddressMailList(user)) {
            if(emailToFind.isSame(email))
                return emailToFind;
        }
        return null;
    }

    public synchronized void updateEmails(String user) {
        BufferedWriter bw;
        try {
            bw = new BufferedWriter(new FileWriter("src/server/"+ user + ".txt"));
            ObservableList<Email> list = getServerMailList();
            Collections.reverse(list);
            for (Email mail : list) {
                if(user.equals(mail.getReceiver())) {
                    bw.write(mail.getId() + "#" + mail.getSender() + "#" + mail.getReceiver() + "#" + mail.getReceivers() + "#" + mail.getSubject() +
                            "#" + mail.getText() + "#" + mail.getDate() + "#" + mail.isRead() + "#" + "\n");
                    bw.flush();
                }
            }
            bw.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void updateUsers() {
        BufferedWriter bw;
        try {
            bw = new BufferedWriter(new FileWriter("src/commonResources/users_list.txt"));
            ObservableList<String> list = getUsersList();
            for(String user : list) {
                bw.write(user + "\n");
                bw.flush();
            }
            bw.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public synchronized void updateIds(String user) {
        for(Email mail: getAddressMailList(user))
            mail.setId(Integer.parseInt(new StringBuilder().append(getUsersList().indexOf(user)+1).append(getAddressMailList(user).indexOf(mail)).toString()));
    }
}