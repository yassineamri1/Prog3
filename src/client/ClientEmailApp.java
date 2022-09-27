package client;

import java.io.*;
import java.net.ConnectException;
import java.net.SocketException;
import java.util.*;
import java.net.Socket;

import commonResources.Email;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXMLLoader;
import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Scene;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.Button;
import javafx.scene.layout.GridPane;
import javafx.scene.Parent;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonType;

public class ClientEmailApp extends Application {

    private static Stage primaryStage;
    private static Stage stage;

    private ClientEmailModel model;
    private ClientEmailController controller;
    private Listener listener;

    @Override
    public void start(Stage stage) {
        Label lblEmail = new Label("Email:");
        ChoiceBox choiceBox = new ChoiceBox();
        String csvUsers = "src/commonResources/users_list.txt";
        String line = "";
        BufferedReader br;
        try {
            br = new BufferedReader(new FileReader(csvUsers));
            while ((line = br.readLine()) != null) {
                choiceBox.getItems().add(line);
            }
            br.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        Label lblServer = new Label("Server:");

        TextField txtServer = new TextField();
        txtServer.setText("localhost");
        txtServer.setDisable(true);

        Button btnOk = new Button();
        btnOk.setText("OK");
        btnOk.setMaxWidth(80);
        btnOk.setOnAction(new EventHandler<ActionEvent>() {

            @Override
            public void handle(ActionEvent event) {
                String email = (String) choiceBox.getValue();
                model = new ClientEmailModel(email);
                listener = new Listener();
                Thread x = new Thread(listener);
                x.start();
            }
        });

        Button btnAnnulla = new Button();
        btnAnnulla.setText("Cancel");
        btnAnnulla.setMaxWidth(80);
        btnAnnulla.setOnAction(new EventHandler<ActionEvent>() {

            @Override
            public void handle(ActionEvent event) {
                Alert alert = new Alert(AlertType.CONFIRMATION);
                alert.setTitle("Confirm");
                alert.setHeaderText("Confirm");
                alert.setContentText("Are you sure do you want to quit?");
                Optional<ButtonType> result = alert.showAndWait();
                if (result.get() == ButtonType.OK) {
                    System.exit(0);
                }
            }
        });

        GridPane root = new GridPane();
        root.setHgap(5);
        root.setVgap(5);
        root.setAlignment(Pos.CENTER);
        root.setPadding(new Insets(5, 5, 5, 5));
        root.add(lblEmail, 0, 0, 1, 1);
        root.add(choiceBox, 1, 0, 1, 1);
        root.add(lblServer, 0, 1, 1, 1);
        root.add(txtServer, 1, 1, 1, 1);
        root.add(btnOk, 0, 2, 1, 2);
        root.add(btnAnnulla, 1, 2, 1, 2);

        setCurrentStage(stage);
        stage.setScene(new Scene(root, 300, 150));
        stage.setTitle("Login");
        stage.setResizable(false);
        stage.show();
    }

    public Stage getCurrentStage() {
        return stage;
    }

    public void setCurrentStage(Stage stage) {
        ClientEmailApp.stage = stage;
    }

    public void openClient() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("ClientEmailView.fxml"));
            Parent root = loader.load();
            controller = loader.getController();
            controller.initApp(this);
            controller.initModel(model);

            getCurrentStage().close();
            primaryStage = new Stage();
            primaryStage.setScene(new Scene(root, 600, 400));
            primaryStage.setTitle("E-mail client");
            primaryStage.show();
            primaryStage.setResizable(false);
            setCurrentStage(primaryStage);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void newMessage(String address, String emailText, String subject) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("ClientEmailForm.fxml"));
            Parent root = loader.load();
            ClientEmailFormController formController = loader.getController();
            formController.initApp(this);
            formController.initModel(model);
            formController.setReceiversText(address);
            formController.setEmailText(emailText);
            formController.setSubjectText(subject);

            stage = new Stage();
            stage.setScene(new Scene(root, 600, 400));
            stage.setTitle("New email");
            stage.initOwner(primaryStage);
            stage.initModality(Modality.WINDOW_MODAL);
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public Listener getListener() {
        return listener;
    }

    @Override
    public void stop() throws IOException {
        listener.stop();
    }

    class Listener implements Runnable {

        private Socket socket;
        private ObjectInputStream inputFromServer;
        private ObjectOutputStream outputToServer;

        @Override
        public void run() {
            try {
                socket = new Socket("localhost", 8182);
                try {
                    inputFromServer = new ObjectInputStream(socket.getInputStream());
                    outputToServer = new ObjectOutputStream(socket.getOutputStream());

                    outputToServer.writeObject("connected");
                    outputToServer.writeObject(model.getAddressClient());
                    String allow = (String) inputFromServer.readObject();
                    switch (allow) {
                        case "refused":
                            Alert alert = new Alert(AlertType.ERROR);
                            alert.setTitle("Connection denied");
                            alert.setHeaderText("Connection denied");
                            alert.setContentText("User already connected!");
                            alert.showAndWait();
                        case "accepted":
                            try {
                                ArrayList<Email> emailList = (ArrayList<Email>) inputFromServer.readObject();
                                ObservableList<Email> oEmailList = FXCollections.observableArrayList(emailList);
                                model.setEmailList(oEmailList);
                                Platform.setImplicitExit(false);
                                Platform.runLater(ClientEmailApp.this::openClient);
                                new Thread( () -> {
                                    while(true) {
                                        try {
                                            Thread.sleep(5000);
                                            listener.refresh();
                                        } catch (Exception e) {
                                            e.printStackTrace();
                                        }
                                    }
                                }).start();
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } catch (ConnectException e) {
                Platform.runLater(() -> {
                    Alert alert = new Alert(AlertType.WARNING);
                    alert.setTitle("Impossible to connect to server");
                    alert.setHeaderText("Impossible to connect to server");
                    alert.setContentText("Server is offline!");
                    alert.showAndWait();
                });
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        public void connect() throws IOException {
            try {
                socket = new Socket("localhost", 8182);
                inputFromServer = new ObjectInputStream(socket.getInputStream());
                outputToServer = new ObjectOutputStream(socket.getOutputStream());
            } catch (SocketException ignored) {
            }
        }

        public void sendEmail(Email email) throws IOException {
            connect();
            try {
                outputToServer.writeObject("send");
                outputToServer.writeObject(model.getAddressClient());
                outputToServer.writeObject(email);
                socket.close();
                stage.close();
            } catch (SocketException e) {
                Platform.runLater(() -> {
                    Alert alert = new Alert(AlertType.WARNING);
                    alert.setTitle("Impossible to connect to server");
                    alert.setHeaderText("Impossible to connect to server");
                    alert.setContentText("Server is offline!");
                    alert.showAndWait();
                });
            }
        }

        public void setReadEmail(Email email) throws IOException {
            connect();
            try {
                outputToServer.writeObject("read");
                outputToServer.writeObject(model.getAddressClient());
                outputToServer.writeObject(email);
                socket.close();
            } catch (SocketException e) {
                Platform.runLater(() -> {
                    Alert alert = new Alert(AlertType.WARNING);
                    alert.setTitle("Impossible to connect to server");
                    alert.setHeaderText("Impossible to connect to server");
                    alert.setContentText("Server is offline!");
                    alert.showAndWait();
                });
            }
        }

        public void deleteEmail(Email email) throws IOException {
            connect();
            try {
                outputToServer.writeObject("delete");
                outputToServer.writeObject(model.getAddressClient());
                outputToServer.writeObject(email);
                socket.close();
            } catch (SocketException e) {
                Platform.runLater(() -> {
                    Alert alert = new Alert(AlertType.WARNING);
                    alert.setTitle("Impossible to connect to server");
                    alert.setHeaderText("Impossible to connect to server");
                    alert.setContentText("Server is offline!");
                    alert.showAndWait();
                });
            }
        }

        public void refresh() throws IOException {
            connect();
            try {
                outputToServer.writeObject("refresh");
                outputToServer.writeObject(model.getAddressClient());
                Integer size = (Integer) inputFromServer.readObject();
                if(model.getEmailList().size() < size) {
                    connect();
                    outputToServer.writeObject("update");
                    outputToServer.writeObject(model.getAddressClient());
                    ArrayList<Email> emailList = (ArrayList<Email>) inputFromServer.readObject();
                    ObservableList<Email> oEmailList = FXCollections.observableArrayList(emailList);
                    model.setEmailList(oEmailList);
                    socket.close();
                    Platform.runLater(() -> {
                        controller.setLsvEmail();
                        Alert alert = new Alert(AlertType.INFORMATION);
                        alert.setTitle("Nuove email ricevute");
                        alert.setHeaderText("Nuove email ricevute");
                        alert.setContentText(model.getAddressClient() + ", hai ricevuto nuove email!");
                        alert.showAndWait();
                    });
                }
            } catch (SocketException | ClassNotFoundException ignored) {

            }
            socket.close();
        }

        public void logout() throws IOException {
            connect();
            try {
                outputToServer.writeObject("disconnected");
                outputToServer.writeObject(model.getAddressClient());
                socket.close();
                primaryStage.close();
                Platform.runLater(() -> {
                    try {
                        new ClientEmailApp().start(new Stage());
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                });
            } catch (SocketException se) {
                primaryStage.close();
                Platform.runLater(() -> {
                    try {
                        new ClientEmailApp().start(new Stage());
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                });
            }
        }

        public void stop() throws IOException {
            connect();
            outputToServer.writeObject("disconnected");
            outputToServer.writeObject(model.getAddressClient());
            socket.close();
        }
    }

    public static void main(String[] args) {
        launch(args);
    }

}