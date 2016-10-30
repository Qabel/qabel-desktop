package de.qabel.desktop.ui.tray;

import javax.swing.*;

public class TrayProxy implements QabelTray {
    private QabelTray instance;

    @Override
    public void install() throws ClassNotFoundException, UnsupportedLookAndFeelException, InstantiationException, IllegalAccessException {
        if (instance != null) {
            instance.install();
        }
    }

    @Override
    public void showNotification(String title, String message) {
        if (instance != null) {
            instance.showNotification(title, message);
        }
    }

    public void setInstance(QabelTray instance) {
        this.instance = instance;
    }
}
