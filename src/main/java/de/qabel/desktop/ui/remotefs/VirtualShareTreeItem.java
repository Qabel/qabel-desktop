package de.qabel.desktop.ui.remotefs;

import de.qabel.desktop.SharingService;
import de.qabel.desktop.config.ShareNotifications;
import de.qabel.desktop.daemon.drop.ShareNotificationMessage;
import de.qabel.desktop.exceptions.QblStorageException;
import de.qabel.desktop.storage.*;
import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.scene.Node;
import javafx.scene.control.TreeItem;

import java.io.IOException;
import java.security.InvalidKeyException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class VirtualShareTreeItem extends TreeItem<BoxObject> {
	private boolean initialized = false;
	private static ExecutorService executorService = Executors.newCachedThreadPool();
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

		notifications.addObserver((o, arg) -> {
			initialized = false;
			Platform.runLater(super.getChildren()::clear);
		});
	}

	@Override
	public boolean isLeaf() {
		return false;
	}

	@Override
	public ObservableList<TreeItem<BoxObject>> getChildren() {
		if (!initialized) {
			executorService.submit(() -> {
				for (ShareNotificationMessage message : notifications.getNotifications()) {
					Platform.runLater(() -> {
						try {
							super.getChildren().add(itemFromNotification(message));
						} catch (Exception e) {
							e.printStackTrace();
						}
					});
				}
			});
			initialized = true;
		}
		return super.getChildren();
	}

	private TreeItem<BoxObject> itemFromNotification(ShareNotificationMessage message) throws IOException, QblStorageException, InvalidKeyException, UnmodifiedException {
		return new ExternalTreeItem(loadFileMetadata(message), message);
	}

	private BoxObject loadFileMetadata(ShareNotificationMessage message) throws IOException, QblStorageException, InvalidKeyException, UnmodifiedException {
		return sharingService.loadFileMetadata(message, readBackend);
	}
}
