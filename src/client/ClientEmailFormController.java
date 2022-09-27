package client;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.Initializable;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.web.HTMLEditor;

public class ClientEmailFormController implements Initializable {

    private ClientEmailApp app;
    private ClientEmailModel model;

    @FXML
    private TextField receivers;

    @FXML
    private TextField subject;

    @FXML
    private HTMLEditor htmlEditor;

    @FXML
    private Button btnSend;

    public void initApp(ClientEmailApp app) {
        this.app = app;
    }

    public void initModel(ClientEmailModel model) {
        if (this.model != null) {
            throw new IllegalStateException("Model can only be initialized once");
        }
        this.model = model;
    }

    public String getReceiversText() {
        return receivers.getText();
    }

    public String getSubjectText() {
        return subject.getText();
    }

    public String getEmailText() {
        return htmlEditor.getHtmlText();
    }

    public void setReceiversText(String address) {
        receivers.setText(address);
    }

    public void setEmailText(String emailText) {
        htmlEditor.setHtmlText(emailText);
    }

    public void setSubjectText(String subjectText) {
        subject.setText(subjectText);
    }

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        btnSend.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                try {
                    if(getReceiversText().trim().isEmpty()) {
                        Alert alert = new Alert(Alert.AlertType.WARNING);
                        alert.setTitle("Impossible to send email");
                        alert.setHeaderText("Impossible to send email");
                        alert.setContentText("Receivers cannot be empty!");
                        alert.showAndWait();
                    } else if(getSubjectText().trim().isEmpty()) {
                        Alert alert = new Alert(Alert.AlertType.WARNING);
                        alert.setTitle("Impossible to send email");
                        alert.setHeaderText("Impossible to send email");
                        alert.setContentText("Subject cannot be empty!");
                        alert.showAndWait();
                    } else
                        app.getListener().sendEmail(model.sendEmail(getReceiversText(), getSubjectText(), getEmailText()));
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
    }
}
