package de.qabel.desktop.ui.tray;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

class TrayIconMouseAdapter extends MouseAdapter {
    private final JPopupMenu popup;
    private final Runnable showApp;
    private final Runnable bringAppToFront;
    private boolean inBound;
    private Timer notificationTimer;

    TrayIconMouseAdapter(Runnable showApp, Runnable bringAppToFront) {
        popup = buildSystemTrayJPopupMenu();
        this.showApp = showApp;
        this.bringAppToFront = bringAppToFront;

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
                inBound = false;
            }

            private boolean isInBounds(MouseEvent e, JPopupMenu popup) {
                return e.getX() < popup.getBounds().getMaxX() &&
                    e.getX() >= popup.getBounds().getMinX() &&
                    e.getY() < popup.getBounds().getMaxY() &&
                    e.getY() >= popup.getBounds().getMinY();
            }
        });
    }

    private void hideIfOutOfBounds() {
        if (popup.isVisible() && !inBound) {
            popup.setVisible(false);
            stopTimer();
        }
        inBound = false;
    }

    private synchronized void startTimer() {
        stopTimer();
        notificationTimer = new Timer();
        notificationTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                hideIfOutOfBounds();
            }
        }, 500, 1500);
    }

    private synchronized void stopTimer() {
        if (notificationTimer != null) {
            notificationTimer.cancel();
            notificationTimer = null;
        }
    }

    public void setInBound(boolean inBound) {
        this.inBound = inBound;
    }

    private JPopupMenu buildSystemTrayJPopupMenu() {
        final JPopupMenu menu = new JPopupMenu();
        final JMenuItem exitMenuItem = new JMenuItem("Exit");

        menu.add(exitMenuItem);
        exitMenuItem.addActionListener(ae -> System.exit(0));
        return menu;
    }

    @Override
    public void mouseReleased(MouseEvent e) {
        if (isSingleLeftClick(e)) {
            bringAppToFront.run();
        } else if (isDoubleLeftClick(e)) {
            showApp.run();
        } else if (isRightClick(e)) {
            Point position = calculatePosition(e);
            popup.setLocation(position.x, position.y);
            popup.setVisible(true);
            startTimer();
        }
    }

    private boolean isRightClick(MouseEvent e) {
        return e.getButton() == MouseEvent.BUTTON3;
    }

    private boolean isSingleLeftClick(MouseEvent e) {
        return e.getButton() == MouseEvent.BUTTON1 && e.getClickCount() == 1;
    }

    private boolean isDoubleLeftClick(MouseEvent e) {
        return e.getButton() == MouseEvent.BUTTON1 && e.getClickCount() == 2;
    }

    private Point calculatePosition(MouseEvent e) {
        Point point = e.getPoint();
        Rectangle bounds = getScreenViewableBounds(getGraphicsDeviceAt(point));
        int x = point.x;
        int y = point.y;

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
        return point;
    }

    private GraphicsDevice getGraphicsDeviceAt(Point pos) {
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

    private Rectangle getScreenViewableBounds(GraphicsDevice gd) {
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
