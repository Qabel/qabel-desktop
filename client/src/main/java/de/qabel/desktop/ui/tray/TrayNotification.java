package de.qabel.desktop.ui.tray;

public class TrayNotification {
    private String title;
    private String content;

    public TrayNotification(String title, String content) {
        this.title = title;
        this.content = content;
    }

    public String getTitle() {
        return title;
    }

    public String getContent() {
        return content;
    }
}
