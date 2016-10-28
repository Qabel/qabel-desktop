package de.qabel.desktop.ui.tray;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;

class TrayIconMouseAdapter extends MouseAdapter {
    private final JPopupMenu popup;
    private final QabelTray qabelTray;
    private boolean visible;

    TrayIconMouseAdapter(QabelTrayImpl qabelTray) {
        this.qabelTray = qabelTray;
        this.popup = qabelTray.popup;
    }

    @Override
    public void mouseReleased(MouseEvent e) {
        if (isDoubleLeftClick(e)) {
            qabelTray.bringAppToFront();
        } else if (isRightClick(e)) {
            Point position = calculatePosition(e);
            visible = !visible;
            qabelTray.showPopup(visible, position.x, position.y);
        }
    }

    private boolean isRightClick(MouseEvent e) {
        return e.getButton() == MouseEvent.BUTTON3;
    }

    private boolean isDoubleLeftClick(MouseEvent e) {
        return e.getClickCount() == 2;
    }

    @Override
    public void mouseExited(MouseEvent e) {
        visible = false;
        popup.setVisible(false);
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
