package commonResources;

import java.io.Serializable;

public class Email implements Serializable {

    private int id;
    private String sender, receiver, receivers, subject, text, date;
    boolean isRead;

    public Email(int id, String sender, String receiver, String receivers, String subject, String text, String date, boolean isRead) {
        this.id = id;
        this.sender = sender;
        this.receiver = receiver;
        this.receivers = receivers;
        this.subject = subject;
        this.text = text;
        this.date = date;
        this.isRead = isRead;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public boolean isRead() {
        return isRead;
    }

    public void setRead(boolean isRead) {
        this.isRead = isRead;
    }

    public String getReceiver() {
        return receiver;
    }

    public void setReceiver(String receiver) {
        this.receiver = receiver;
    }

    public String getReceivers() {
        return receivers;
    }

    public void setReceivers(String receivers) {
        this.receivers = receivers;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getSender() {
        return sender;
    }

    public boolean isSame(Email email) {
        if(this.id == email.getId())
            return true;
        else
            return false;
    }

    @Override
    public String toString() {
        return subject;
    }
}
