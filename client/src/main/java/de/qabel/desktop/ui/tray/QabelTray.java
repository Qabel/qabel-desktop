package de.qabel.desktop.ui.tray;

import javax.swing.*;

public interface QabelTray {
    void install() throws ClassNotFoundException, UnsupportedLookAndFeelException, InstantiationException, IllegalAccessException;
    void showNotification(String title, String message);
}
