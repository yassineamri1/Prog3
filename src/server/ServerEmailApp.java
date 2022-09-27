package server;

import commonResources.Email;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.ArrayList;

public class ServerEmailApp extends Application {

    private ServerEmailModel model;
    private ServerEmailController controller;

    private ServerSocket serverSocket;
    private ObjectInputStream inputFromClient;
    private ObjectOutputStream outputToClient;

    private String action, user;
    private ArrayList<String> onlineUsersList = new ArrayList<>();

    public ArrayList<String> getOnlineUsersList() {
        return onlineUsersList;
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("ServerEmailView.fxml"));
        Parent root = loader.load();
        controller = loader.getController();
        model = new ServerEmailModel();
        controller.initApp(this);
        controller.initModel(model);

        primaryStage.setScene(new Scene(root, 600, 400));
        primaryStage.setTitle("E-mail server");
        primaryStage.setResizable(false);
        primaryStage.show();

        new Thread( () -> {
            try {
                serverSocket = new ServerSocket(8182);

                Platform.runLater( () -> {
                    controller.addLog("Server is online");
                });

                while (true) {
                    Socket socket = serverSocket.accept();
                    new Thread(new ClientHandler(socket)).start();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }

    class ClientHandler implements Runnable {

        private Socket socket;

        public ClientHandler(Socket socket) {
            this.socket = socket;
        }

        @Override
        public void run () {
            try {
                outputToClient = new ObjectOutputStream(socket.getOutputStream());
                inputFromClient = new ObjectInputStream(socket.getInputStream());
                action = (String) inputFromClient.readObject();
                switch (action) {
                    case "connected":
                        user = (String) inputFromClient.readObject();
                        if (!onlineUsersList.contains(user)) {
                            outputToClient.writeObject("accepted");
                            outputToClient.writeObject(model.getAddressMailList(user));
                            Platform.runLater(() -> {
                                controller.addLog(user + " connected");
                                onlineUsersList.add(user);
                            });
                        } else {
                            outputToClient.writeObject("refused");
                        }
                        break;
                    case "disconnected":
                        user = (String) inputFromClient.readObject();
                        socket.close();
                        Platform.runLater(() -> {
                            controller.addLog(user + " disconnected");
                            onlineUsersList.remove(user);
                        });

                        break;
                    case "read": {
                        user = (String) inputFromClient.readObject();
                        Email email = (Email) inputFromClient.readObject();
                        model.setReadEmail(user, model.findEmail(email, user));
                        Platform.runLater(() -> {
                            model.updateEmails(user);
                            controller.setLsvEmails();
                        });
                        break;
                    }
                    case "send": {
                        user = (String) inputFromClient.readObject();
                        Email email = (Email) inputFromClient.readObject();
                        String[] receivers = email.getReceivers().trim().split("\\s*,\\s*");
                        for (String receiver : receivers) {
                            email.setReceiver(receiver);
                            if (!model.getServerMailMap().containsKey(receiver))
                                email.setReceivers(email.getReceivers().trim().replace(", " + receiver, "")
                                        .replace(receiver + ",", "")
                                        .replace(receiver, ""));
                            model.sendMail(email, receiver);
                        }
                        Platform.runLater(() -> {
                            model.updateEmails(user);
                            controller.setLsvEmails();
                        });
                        break;
                    }
                    case "delete": {
                        user = (String) inputFromClient.readObject();
                        Email email = (Email) inputFromClient.readObject();
                        model.deleteEmail(user, model.findEmail(email, user));
                        Platform.runLater(() -> {
                            model.updateIds(user);
                            model.updateEmails(user);
                            controller.setLsvEmails();
                        });
                        break;
                    }

                    case "refresh": {
                        user = (String) inputFromClient.readObject();
                        outputToClient.writeObject(model.getAddressMailList(user).size());
                        break;
                    }

                    case "update": {
                        user = (String) inputFromClient.readObject();
                        outputToClient.writeObject(model.getAddressMailList(user));
                        break;
                    }
                }
                socket.shutdownInput();
                socket.shutdownOutput();
            } catch (SocketException e) {

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}