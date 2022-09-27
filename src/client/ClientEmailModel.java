package client;

import commonResources.Email;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

public class ClientEmailModel {

    private String addressClient;
    private ObservableList<Email> emailList = FXCollections.observableArrayList();
    private final ObjectProperty<Email> currentEmail = new SimpleObjectProperty<>(null);

    public ClientEmailModel(String addressClient) {
        this.addressClient = addressClient;
    }

    public String getAddressClient() {
        return addressClient;
    }

    public ObservableList<Email> getEmailList() {
        return emailList;
    }

    public void setEmailList(ObservableList<Email> emailList) {
        this.emailList = emailList;
    }

    public ObjectProperty<Email> currentReceivedEmailProperty() {
        return currentEmail;
    }

    public final Email getCurrentReceivedEmail() {
        return currentEmail.get();
    }

    public final void setCurrentReceivedEmail(Email email) {
        currentEmail.set(email);
    }

    public Email sendEmail(String receivers, String subject, String text) {
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date date = new Date();
        return new Email(0, getAddressClient(), "", receivers, subject, text, dateFormat.format(date), false);
    }
}
