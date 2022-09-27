package server;

public class Log {
    private String logText, creationDate;

    public Log(String logText, String creationDate) {
        this.logText = logText;
        this.creationDate = creationDate;
    }

    public String getLogText() {
        return logText;
    }

    public void setLogText(String logText) {
        this.logText = logText;
    }

    public String getCreationDate() {
        return creationDate;
    }

    public void setCreationDate(String creationDate) {
        this.creationDate = creationDate;
    }

    @Override
    public String toString() {
        return creationDate + " - " + logText;
    }
}