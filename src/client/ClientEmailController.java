package client;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;
import commonResources.Email;

import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.Initializable;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.web.WebView;

public class ClientEmailController implements Initializable {

    private ClientEmailApp app;
    private ClientEmailModel model;

    @FXML
    private Button btnNew;

    @FXML
    private Button btnBack;

    @FXML
    private Button btnReply;

    @FXML
    private Button btnReplyAll;

    @FXML
    private Button btnForward;

    @FXML
    private Button btnDelete;

    @FXML
    private Button btnLogout;

    @FXML
    private Label lblAddress;

    @FXML
    private Button btnLogout2;

    @FXML
    private Label lblAddress2;

    @FXML
    private ToolBar tbarHome;

    @FXML
    private ToolBar tbarActions;

    @FXML
    private WebView wvEmailText;

    @FXML
    private ListView<Email> lsvReceivedEmails;

    public void initApp(ClientEmailApp app) {
        this.app = app;
    }

    public void initModel(ClientEmailModel model) {
        if (this.model != null) {
            throw new IllegalStateException("Model can only be initialized once");
        }
        this.model = model;
        wvEmailText.setVisible(false);
        tbarActions.setVisible(false);
        setLblAddress();
        setLsvEmail();
        updateCell();

        lsvReceivedEmails.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) ->
                model.setCurrentReceivedEmail(newSelection));

        model.currentReceivedEmailProperty().addListener((obs, oldEmail, newEmail) -> {
            if (newEmail == null) {
                lsvReceivedEmails.getSelectionModel().clearSelection();
            } else {
                lsvReceivedEmails.getSelectionModel().select(newEmail);
            }
        });

        lsvReceivedEmails.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                Email selected = lsvReceivedEmails.getSelectionModel().getSelectedItem();
                if(selected != null) {
                    if(!selected.isRead()) {
                        selected.setRead(true);
                        try {
                            app.getListener().setReadEmail(selected);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                    updateCell();
                    tbarHome.setVisible(false);
                    tbarActions.setVisible(true);
                    wvEmailText.setVisible(true);
                    if(selected.getReceivers().replaceAll("[^,]", "").length() < 1)
                        btnReplyAll.setDisable(true);
                    else
                        btnReplyAll.setDisable(false);

                    StringBuilder sb = new StringBuilder();
                    sb.append("<html><body style='font-size: 13; font-family: Segoe UI'>")
                            .append("<div style='font-size: 17px; font-weight: bold; padding-right: 7px'>" + selected.getSubject() + "</div>")
                            .append("to: " + selected.getReceivers() + "<br>")
                            .append("<div style='color: grey'>from: " + selected.getSender() + "</div>")
                            .append("<hr>" + selected.getText())
                            .append("</body></html>");
                    String text = sb.toString();
                    wvEmailText.getEngine().loadContent(text);
                }
            }
        });
        setImages();
    }

    public void setEmailList(ObservableList<Email> emailList) {
        model.setEmailList(emailList);
    }

    public void setLblAddress() {
        lblAddress.setText(model.getAddressClient());
        lblAddress2.setText(model.getAddressClient());

    }

    public void setLsvEmail() {
        lsvReceivedEmails.setItems(model.getEmailList());
    }

    public void setImages() {
        Image imageNew = new Image("icons/pencil-2x.png");
        ImageView ivNew = new ImageView(imageNew);
        btnNew.setGraphic(ivNew);

        Image imageLogout = new Image("icons/account-logout-2x.png");
        ImageView ivLogout = new ImageView(imageLogout);
        ImageView ivLogout2 = new ImageView(imageLogout);
        btnLogout.setGraphic(ivLogout);
        btnLogout2.setGraphic(ivLogout2);

        Image imageBack = new Image("icons/chevron-left-2x.png");
        ImageView ivBack = new ImageView(imageBack);
        btnBack.setGraphic(ivBack);

        Image imageReply = new Image("icons/reply-2x.png");
        ImageView ivReply = new ImageView(imageReply);
        btnReply.setGraphic(ivReply);

        Image imageReplyAll = new Image("icons/reply-all-2x.png");
        ImageView ivReplyAll = new ImageView(imageReplyAll);
        btnReplyAll.setGraphic(ivReplyAll);

        Image imageForward = new Image("icons/arrow-thick-right-2x.png");
        ImageView ivForward = new ImageView(imageForward);
        btnForward.setGraphic(ivForward);

        Image imageDelete = new Image("icons/trash-2x.png");
        ImageView ivDelete = new ImageView(imageDelete);
        btnDelete.setGraphic(ivDelete);
    }

    public void updateCell() {
        lsvReceivedEmails.setCellFactory(list -> {
            ListCell<Email> cell = new ListCell<Email>() {
                @Override
                protected void updateItem(Email item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty || item == null) {
                        setGraphic(null);
                        setStyle(null);
                        setText(null);
                    } else {
                        setText(item.getSubject());
                        if (!item.isRead()) {
                            this.setStyle("-fx-font-weight: bold");
                        }
                    }
                }
            };
            return cell;
        });
    }

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        btnNew.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                app.newMessage("", "", "");
            }
        });

        btnLogout.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                try {
                    app.getListener().logout();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });

        btnLogout2.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                try {
                    app.getListener().logout();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });

        btnBack.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                tbarActions.setVisible(false);
                tbarHome.setVisible(true);
                wvEmailText.setVisible(false);
            }
        });

        btnReply.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                Email email = lsvReceivedEmails.getSelectionModel().getSelectedItem();
                app.newMessage(email.getSender(), "", "Re: " + email.getSubject());
            }
        });

        btnReplyAll.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                Email email = lsvReceivedEmails.getSelectionModel().getSelectedItem();
                String receivers =  email.getSender() + ", " + email.getReceivers().trim().replace(", " + model.getAddressClient(), "")
                        .replace(model.getAddressClient() + ",", "")
                        .replace(model.getAddressClient(), "");
                app.newMessage(receivers, "", "Re: " + email.getSubject());
            }
        });

        btnForward.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                Email email = lsvReceivedEmails.getSelectionModel().getSelectedItem();
                app.newMessage("", email.getText(), email.getSubject());
            }
        });

        btnDelete.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                try {
                    Email email = lsvReceivedEmails.getSelectionModel().getSelectedItem();
                    model.getEmailList().remove(email);
                    app.getListener().deleteEmail(email);
                    tbarActions.setVisible(false);
                    tbarHome.setVisible(true);
                    wvEmailText.setVisible(false);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
    }
}
