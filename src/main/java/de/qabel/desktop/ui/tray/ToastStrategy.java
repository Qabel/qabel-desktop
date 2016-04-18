package de.qabel.desktop.ui.tray;

import java.awt.*;

public interface ToastStrategy {
    void showNotification(String title, String message, TrayIcon trayIcon);
}
