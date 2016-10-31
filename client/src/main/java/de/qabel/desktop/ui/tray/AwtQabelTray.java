package de.qabel.desktop.ui.tray;

import javafx.application.Platform;
import javafx.stage.Stage;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.net.URL;

public class AwtQabelTray implements QabelTray {
    private static final Logger logger = LoggerFactory.getLogger(AwtQabelTray.class);
    private final Stage primaryStage;
    private ToastStrategy toastStrategy;
    private TrayIcon icon;

    public AwtQabelTray(Stage primaryStage, ToastStrategy toastStrategy) {
        this.primaryStage = primaryStage;
        this.toastStrategy = toastStrategy;
    }

    private void showApp() {
        Platform.runLater(() -> {
            if (!primaryStage.isShowing()) {
                primaryStage.show();
            }
            primaryStage.toFront();
        });
    }

    private void bringAppToFront() {
        Platform.runLater(primaryStage::toFront);
    }

    @Override
    public void install() {
        if (!SystemTray.isSupported()) {
            return;
        }
        SystemTray tray = SystemTray.getSystemTray();

        primaryStage.setOnCloseRequest(arg0 -> primaryStage.hide());
        icon = buildTrayIcon();
        trayIconListener(icon);

        try {
            tray.add(icon);
        } catch (AWTException e) {
            logger.error("failed to add tray icon: " + e.getMessage(), e);
        }
    }

    @NotNull
    private TrayIcon buildTrayIcon() {
        URL url = System.class.getResource("/logo-invert_small.png");
        Image img = Toolkit.getDefaultToolkit().getImage(url);
        icon = new TrayIcon(img, "Qabel");
        icon.setImageAutoSize(true);
        return icon;
    }

    @Override
    public void showNotification(TrayNotification trayNotification) {
        toastStrategy.showNotification(trayNotification.getTitle(), trayNotification.getContent(), icon);
    }

    private void trayIconListener(TrayIcon icon) {
        TrayIconMouseAdapter mouseAdapter = new TrayIconMouseAdapter(this::showApp, this::bringAppToFront);
        icon.addMouseListener(mouseAdapter);
        icon.addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseMoved(MouseEvent e) {
                mouseAdapter.setInBound(true);
            }
        });
    }
}
