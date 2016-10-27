package de.qabel.desktop.config;

import de.qabel.desktop.daemon.drop.ShareNotificationMessage;

import java.io.Serializable;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Observable;

public class ShareNotifications extends Observable implements Serializable {
    private static final long serialVersionUID = -5929617089425021709L;
    private List<ShareNotificationMessage> notifications = new LinkedList<>();

    public void add(ShareNotificationMessage notification) {
        notifications.add(notification);
        setChanged();
        notifyObservers(notification);
    }

    public void remove(ShareNotificationMessage notification) {
        notifications.remove(notification);
        setChanged();
        notifyObservers();
    }

    public List<ShareNotificationMessage> getNotifications() {
        return Collections.unmodifiableList(notifications);
    }
}
