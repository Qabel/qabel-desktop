package de.qabel.desktop.ui.tray;

public class TrayProxy implements QabelTray {
    private QabelTray instance;

    @Override
    public void install() {
        if (instance != null) {
            instance.install();
        }
    }

    @Override
    public void showNotification(TrayNotification notification) {
        if (instance != null) {
            instance.showNotification(notification);
        }
    }

    public void setInstance(QabelTray instance) {
        this.instance = instance;
    }
}
