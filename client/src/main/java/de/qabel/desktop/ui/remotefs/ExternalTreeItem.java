package de.qabel.desktop.ui.remotefs;

import de.qabel.box.storage.BoxObject;
import de.qabel.desktop.daemon.drop.ShareNotificationMessage;
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
