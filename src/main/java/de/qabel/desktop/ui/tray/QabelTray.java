package de.qabel.desktop.ui.tray;

import javax.swing.*;
import java.awt.*;

public interface QabelTray {

    void bringAppToFront();

    void showPopup(boolean visible, int x, int y);

    void install() throws ClassNotFoundException, UnsupportedLookAndFeelException, InstantiationException, IllegalAccessException;

    void showNotification(String title, String message, TrayIcon icon);

    void showNotification(String title, String message);
}
