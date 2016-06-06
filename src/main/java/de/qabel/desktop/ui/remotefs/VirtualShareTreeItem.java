package de.qabel.desktop.ui.remotefs;

import de.qabel.box.storage.AuthenticatedDownloader;
import de.qabel.box.storage.BoxObject;
import de.qabel.box.storage.UnmodifiedException;
import de.qabel.box.storage.exceptions.QblStorageException;
import de.qabel.box.storage.exceptions.QblStorageNotFound;
import de.qabel.desktop.SharingService;
import de.qabel.desktop.config.ShareNotifications;
import de.qabel.desktop.daemon.drop.ShareNotificationMessage;
import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.scene.Node;
import javafx.scene.control.TreeItem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.security.InvalidKeyException;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class VirtualShareTreeItem extends TreeItem<BoxObject> {
    private static Logger logger = LoggerFactory.getLogger(VirtualShareTreeItem.class);
    private boolean initialized;
    private static ExecutorService executorService = Executors.newSingleThreadExecutor();
    private ShareNotifications notifications;
    private SharingService sharingService;
    private AuthenticatedDownloader readBackend;

    public VirtualShareTreeItem(
            SharingService sharingService,
            AuthenticatedDownloader readBackend,
            ShareNotifications notifications,
            BoxObject value,
            Node graphic
    ) {
        super(value, graphic);
        this.notifications = notifications;
        this.readBackend = readBackend;
        this.sharingService = sharingService;

        notifications.addObserver((o, arg) -> executorService.submit(() -> {
            initialized = false;
            reload();
        }));
    }

    @Override
    public boolean isLeaf() {
        return getChildren().isEmpty();
    }

    @Override
    public ObservableList<TreeItem<BoxObject>> getChildren() {
        reload();
        return super.getChildren();
    }

    private synchronized void reload() {
        if (!initialized) {
            executorService.submit(() -> {
                if (!super.getChildren().isEmpty()) {
                    Platform.runLater(super.getChildren()::clear);
                }
                List<ShareNotificationMessage> removedShares = new LinkedList<>();
                for (ShareNotificationMessage message : notifications.getNotifications()) {
                    try {
                        final TreeItem<BoxObject> item = itemFromNotification(message);
                        Platform.runLater(() -> super.getChildren().add(item));
                    } catch (QblStorageNotFound e) {
                        removedShares.add(message);
                    } catch (Exception e) {
                        logger.error("failed to load FM", e);
                    }
                }
                for (ShareNotificationMessage message : removedShares) {
                    notifications.remove(message);
                }
            });
            initialized = true;
        }
    }

    private TreeItem<BoxObject> itemFromNotification(ShareNotificationMessage message) throws IOException, QblStorageException, InvalidKeyException, UnmodifiedException {
        return new ExternalTreeItem(loadFileMetadata(message), message);
    }

    private BoxObject loadFileMetadata(ShareNotificationMessage message) throws IOException, QblStorageException, InvalidKeyException, UnmodifiedException {
        return sharingService.loadFileMetadata(message, readBackend);
    }

    public synchronized void refresh() {
        for (ShareNotificationMessage notification : notifications.getNotifications()) {
            try {
                sharingService.loadFileMetadata(notification, readBackend);
                initialized = false;
                reload();
                return;
            } catch (IOException | InvalidKeyException | QblStorageException | UnmodifiedException ignored) {
            }
        }
    }
}
