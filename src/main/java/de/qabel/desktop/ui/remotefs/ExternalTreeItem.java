package de.qabel.desktop.ui.remotefs;

import de.qabel.desktop.daemon.drop.ShareNotificationMessage;
import de.qabel.desktop.storage.BoxObject;
import javafx.scene.control.TreeItem;

public class ExternalTreeItem extends TreeItem<BoxObject> {
    private final ShareNotificationMessage notification;

    public ExternalTreeItem(BoxObject value, ShareNotificationMessage notification) {
        super(value);
        this.notification = notification;
    }

    public ShareNotificationMessage getNotification() {
        return notification;
    }
}
