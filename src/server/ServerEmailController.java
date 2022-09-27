package server;

import commonResources.Email;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Optional;
import java.util.ResourceBundle;

public class ServerEmailController implements Initializable {

    private ServerEmailApp app;
    private ServerEmailModel model;

    @FXML
    private Tab tabEmails;

    @FXML
    private Tab tabLog;

    @FXML
    private Tab tabUsers;

    @FXML
    private ToolBar tbarLog;

    @FXML
    private ToolBar tbarUsers;

    @FXML
    private Button btnDeleteLog;

    @FXML
    private Button btnAddUser;

    @FXML
    private Button btnRemoveUser;

    @FXML
    private ListView<Email> lsvEmails;

    @FXML
    private ListView<Log> lsvLog;

    @FXML
    private ListView<String> lsvUsers;

    public void initApp(ServerEmailApp app) {
        this.app = app;
    }

    public void initModel(ServerEmailModel model) {
        if (this.model != null) {
            throw new IllegalStateException("Model can only be initialized once");
        }
        this.model = model;

        setLsvEmails();
        setLsvLog();
        setLsvUsers();

        lsvLog.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) ->
                model.setCurrentLog(newSelection));

        model.currentLogProperty().addListener((obs, oldLog, newLog) -> {
            if (newLog == null) {
                lsvLog.getSelectionModel().clearSelection();
            } else {
                lsvLog.getSelectionModel().select(newLog);
            }
        });

        lsvEmails.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) ->
                model.setCurrentEmail(newSelection));

        model.currentEmailProperty().addListener((obs, oldEmail, newEmail) -> {
            if (newEmail == null) {
                lsvEmails.getSelectionModel().clearSelection();
            } else {
                lsvEmails.getSelectionModel().select(newEmail);
            }
        });

        lsvUsers.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) ->
                model.setCurrentUser(newSelection));

        model.currentUserProperty().addListener((obs, oldUser, newUser) -> {
            if (newUser == null) {
                lsvUsers.getSelectionModel().clearSelection();
            } else {
                lsvUsers.getSelectionModel().select(newUser);
            }
        });

        tbarUsers.setVisible(false);
        setImages();
    }

    public void addLog(String text) {
        model.addLog(text);
    }

    public void deleteLog() {
        lsvLog.getItems().clear();
    }

    public void setLsvEmails() {
        lsvEmails.setItems(model.getServerMailList());
    }

    public void setLsvLog() {
        lsvLog.setItems(model.getLogList());
    }

    public void setLsvUsers() {
        lsvUsers.setItems(model.getUsersList());
    }

    public void setImages() {
        Image imageLog = new Image("icons/list-2x.png");
        ImageView ivLog = new ImageView(imageLog);
        tabLog.setGraphic(ivLog);

        Image imageEmails = new Image("icons/envelope-closed-2x.png");
        ImageView ivEmails = new ImageView(imageEmails);
        tabEmails.setGraphic(ivEmails);

        Image imageUsers = new Image("icons/people-2x.png");
        ImageView ivUsers = new ImageView(imageUsers);
        tabUsers.setGraphic(ivUsers);

        Image imageDelete = new Image("icons/trash-2x.png");
        ImageView ivDelete = new ImageView(imageDelete);
        btnDeleteLog.setGraphic(ivDelete);

        Image imageAdd = new Image("icons/plus-2x.png");
        ImageView ivAdd = new ImageView(imageAdd);
        btnAddUser.setGraphic(ivAdd);

        Image imageRemove = new Image("icons/minus-2x.png");
        ImageView ivRemove = new ImageView(imageRemove);
        btnRemoveUser.setGraphic(ivRemove);
    }

    public void initialize(URL url, ResourceBundle rb) {
        tabUsers.setOnSelectionChanged(new EventHandler<Event>() {
            @Override
            public void handle(Event event) {
                tbarLog.setVisible(false);
                tbarUsers.setVisible(true);
            }
        });

        tabEmails.setOnSelectionChanged(new EventHandler<Event>() {
            @Override
            public void handle(Event event) {
                tbarLog.setVisible(false);
                tbarUsers.setVisible(false);
            }
        });

        tabLog.setOnSelectionChanged(new EventHandler<Event>() {
            @Override
            public void handle(Event event) {
                tbarLog.setVisible(true);
                tbarUsers.setVisible(false);
            }
        });

        btnDeleteLog.setOnAction(new EventHandler<ActionEvent>() {

            @Override
            public void handle(ActionEvent event) {
                deleteLog();
            }
        });

        btnAddUser.setOnAction(new EventHandler<ActionEvent>() {

            @Override
            public void handle(ActionEvent event) {
                TextInputDialog dialog = new TextInputDialog();
                dialog.setTitle("Add new user");
                dialog.setHeaderText("Add new user");
                dialog.setContentText("Insert new user email address:");

                Optional<String> result = dialog.showAndWait();
                if (result.isPresent()) {
                    ArrayList<Email> mailList = new ArrayList<>();
                    model.getUsersList().add(result.get());
                    model.getServerMailMap().put(result.get(), mailList);
                    model.updateUsers();
                    String email = "src/server/" + result.get() + ".txt";
                    File file = new File(email);
                    try {
                        file.createNewFile();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        });

        btnRemoveUser.setOnAction(new EventHandler<ActionEvent>() {

            @Override
            public void handle(ActionEvent event) {
                String user = lsvUsers.getSelectionModel().getSelectedItem();
                if(!app.getOnlineUsersList().contains(user)) {
                    model.getServerMailMap().remove(user);
                    model.getUsersList().remove(user);
                    model.updateUsers();
                    String email = "src/server/" + user + ".txt";
                    File file = new File(email);
                    file.delete();
                } else {
                    Alert alert = new Alert(Alert.AlertType.WARNING);
                    alert.setTitle("Impossible to remove user");
                    alert.setHeaderText("Impossible to remove user");
                    alert.setContentText("Current user is already online!");
                    alert.showAndWait();
                }
            }
        });
    }
}