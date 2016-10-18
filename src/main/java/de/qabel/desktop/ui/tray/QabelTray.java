package de.qabel.desktop.ui.tray;

import javafx.application.Platform;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.net.URL;
import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

public class QabelTray {
    private static final Logger logger = LoggerFactory.getLogger(QabelTray.class);
    private final Stage primaryStage;
    private boolean visible;
    private boolean inBound;
    private JPopupMenu popup;
    private SystemTray tray;
    private TrayIcon icon;
    private ToastStrategy toastStrategy;

    public QabelTray(Stage primaryStage, ToastStrategy toastStrategy) {
        this.primaryStage = primaryStage;
        this.toastStrategy = toastStrategy;
    }

    public void install() throws ClassNotFoundException, UnsupportedLookAndFeelException, InstantiationException, IllegalAccessException {
        if (!SystemTray.isSupported()) {
            return;
        }

        tray = SystemTray.getSystemTray();
        primaryStage.setOnCloseRequest(arg0 -> primaryStage.hide());
        popup = buildSystemTrayJPopupMenu(primaryStage);
        URL url = System.class.getResource("/logo-invert_small.png");
        Image img = Toolkit.getDefaultToolkit().getImage(url);
        icon = new TrayIcon(img, "Qabel");

        icon.setImageAutoSize(true);
        trayIconListener(popup, icon);

        try {
            tray.add(icon);
        } catch (AWTException e) {
            logger.error("failed to add tray icon: " + e.getMessage(), e);
        }
    }

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
                        visible = false;
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

        popup.addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseMoved(MouseEvent e) {
                inBound = true;
            }
        });

        icon.addMouseListener(new MouseAdapter() {

            int x,y;
            private void calculatePosition(MouseEvent e){
                Point point = e.getPoint();
                Rectangle bounds = getScreenViewableBounds(getGraphicsDeviceAt(point));
                x = point.x;
                y = point.y;
                if (y < bounds.y) {
                    y = bounds.y;
                } else if (y > bounds.y + bounds.height) {
                    y = bounds.y + bounds.height;
                }
                if (x < bounds.x) {
                    x = bounds.x;
                } else if (x > bounds.x + bounds.width) {
                    x = bounds.x + bounds.width;
                }
                if (x + popup.getWidth() > bounds.x + bounds.width) {
                    x = bounds.x + bounds.width - popup.getWidth();
                }
                if (y + popup.getWidth() > bounds.y + bounds.height) {
                    y = bounds.y + bounds.height - popup.getHeight();
                }

            }

            private void showPopup(){
                calculatePosition(event);
                visible = !visible;
                if (visible) {
                    popup.setLocation(x, y);
                }
                popup.setVisible(visible);
            }

            private void bringAppToFront() {

                Platform.runLater(() -> {
                    if(!primaryStage.isShowing()){
                        primaryStage.show();
                    }else{
                        primaryStage.toFront();
                    }
                });
            }

            MouseEvent event;

            @Override
            public void mouseReleased(MouseEvent e) {
                event = e;
                if(isDoubleLeftClicked(e)){
                    bringAppToFront();
                }else if(e.getButton() == MouseEvent.BUTTON3){
                    showPopup();
                }
            }

            private boolean isDoubleLeftClicked(MouseEvent e) {
                return e.getClickCount() == 2;
            }

            @Override
            public void mouseExited(MouseEvent e) {
                visible = false;
                popup.setVisible(visible);
            }
        });

        popup.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseExited(MouseEvent e) {
                if (e.getX() < popup.getBounds().getMaxX() &&
                    e.getX() >= popup.getBounds().getMinX() &&
                    e.getY() < popup.getBounds().getMaxY() &&
                    e.getY() >= popup.getBounds().getMinY()) {
                    return;
                }
                visible = false;
                popup.setVisible(visible);
            }
        });
    }



    protected JPopupMenu buildSystemTrayJPopupMenu(Stage primaryStage) throws ClassNotFoundException, UnsupportedLookAndFeelException, InstantiationException, IllegalAccessException {
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

    public GraphicsDevice getGraphicsDeviceAt(Point pos) {

        GraphicsDevice device = null;
        GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
        GraphicsDevice lstGDs[] = ge.getScreenDevices();
        ArrayList<GraphicsDevice> lstDevices = new ArrayList<>(lstGDs.length);

        for (GraphicsDevice gd : lstGDs) {
            GraphicsConfiguration gc = gd.getDefaultConfiguration();
            Rectangle screenBounds = gc.getBounds();
            if (screenBounds.contains(pos)) {
                lstDevices.add(gd);
            }
        }

        if (lstDevices.size() == 1) {
            device = lstDevices.get(0);
        }
        return device;
    }

    public Rectangle getScreenViewableBounds(GraphicsDevice gd) {

        Rectangle bounds = new Rectangle(0, 0, 0, 0);

        if (gd != null) {
            GraphicsConfiguration gc = gd.getDefaultConfiguration();
            bounds = gc.getBounds();
            Insets insets = Toolkit.getDefaultToolkit().getScreenInsets(gc);

            bounds.x += insets.left;
            bounds.y += insets.top;
            bounds.width -= insets.left + insets.right;
            bounds.height -= insets.top + insets.bottom;
        }
        return bounds;

    }

}
