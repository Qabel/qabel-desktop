package de.qabel.desktop.ui.tray;

import java.awt.*;

public class AwtToast implements ToastStrategy {
    @Override
    public void showNotification(String title, String message, TrayIcon trayIcon) {
        trayIcon.displayMessage(title, message, TrayIcon.MessageType.INFO);
    }
}
