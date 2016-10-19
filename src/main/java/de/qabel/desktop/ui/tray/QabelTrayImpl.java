package de.qabel.desktop.ui.tray;

import javafx.application.Platform;
import javafx.stage.Stage;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.net.URL;
import java.util.Timer;
import java.util.TimerTask;

public class QabelTrayImpl implements QabelTray {
    private static final Logger logger = LoggerFactory.getLogger(QabelTrayImpl.class);
    private final Stage primaryStage;
    private boolean visible;
    private boolean inBound;
    private ToastStrategy toastStrategy;
    public JPopupMenu popup;
    private TrayIcon icon;

    public QabelTrayImpl(Stage primaryStage, ToastStrategy toastStrategy) {
        this.primaryStage = primaryStage;
        this.toastStrategy = toastStrategy;
    }

    @Override
    public void bringAppToFront() {
        Platform.runLater(() -> {
            if (!primaryStage.isShowing()) {
                primaryStage.show();
            }
            primaryStage.toFront();
        });
    }

    @Override
    public void showPopup(boolean visible, int x, int y) {
        if (visible) {
            popup.setLocation(x, y);
        }
        popup.setVisible(visible);
    }

    @Override
    public void install() throws ClassNotFoundException, UnsupportedLookAndFeelException, InstantiationException, IllegalAccessException {
        if (!SystemTray.isSupported()) {
            return;
        }
        SystemTray tray = SystemTray.getSystemTray();

        primaryStage.setOnCloseRequest(arg0 -> primaryStage.hide());
        popup = buildSystemTrayJPopupMenu(primaryStage);
        icon = getTrayIcon();
        trayIconListener(popup, icon);

        try {
            tray.add(icon);
        } catch (AWTException e) {
            logger.error("failed to add tray icon: " + e.getMessage(), e);
        }
    }

    @NotNull
    private TrayIcon getTrayIcon() {
        URL url = System.class.getResource("/logo-invert_small.png");
        Image img = Toolkit.getDefaultToolkit().getImage(url);
        icon = new TrayIcon(img, "Qabel");
        icon.setImageAutoSize(true);
        return icon;
    }

    @Override
    public void showNotification(String title, String message) {
        toastStrategy.showNotification(title, message, icon);
    }

    private void trayIconListener(final JPopupMenu popup, TrayIcon icon) {
        Timer notificationTimer = new Timer();
        notificationTimer.schedule(
            new TimerTask() {
                @Override
                public void run() {
                    if (visible && !inBound) {
                        popup.setVisible(false);
                    }
                    inBound = false;
                }
            }, 250, 1500
        );

        icon.addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseMoved(MouseEvent e) {
                inBound = true;
            }
        });
        icon.addMouseListener(new TrayIconMouseAdapter(this));

        popup.addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseMoved(MouseEvent e) {
                inBound = true;
            }
        });

        popup.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseExited(MouseEvent e) {
                if (isInBounds(e, popup)) {
                    return;
                }
                visible = !visible;
                popup.setVisible(false);
            }

            private boolean isInBounds(MouseEvent e, JPopupMenu popup) {
                return e.getX() < popup.getBounds().getMaxX() &&
                    e.getX() >= popup.getBounds().getMinX() &&
                    e.getY() < popup.getBounds().getMaxY() &&
                    e.getY() >= popup.getBounds().getMinY();
            }
        });
    }

    private JPopupMenu buildSystemTrayJPopupMenu(Stage primaryStage) throws ClassNotFoundException, UnsupportedLookAndFeelException, InstantiationException, IllegalAccessException {
        final JPopupMenu menu = new JPopupMenu();
        final JMenuItem showMenuItem = new JMenuItem("Show");
        final JMenuItem exitMenuItem = new JMenuItem("Exit");

        menu.add(showMenuItem);
        menu.addSeparator();
        menu.add(exitMenuItem);
        showMenuItem.addActionListener(ae -> Platform.runLater(primaryStage::show));
        exitMenuItem.addActionListener(ae -> System.exit(0));

        return menu;
    }

}
