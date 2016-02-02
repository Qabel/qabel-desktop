package de.qabel.desktop.daemon.sync.worker;

import de.qabel.desktop.daemon.management.BoxSyncBasedUpload;
import de.qabel.desktop.daemon.management.TransferManager;

import de.qabel.desktop.config.BoxSyncConfig;
import de.qabel.desktop.daemon.management.*;
import de.qabel.desktop.daemon.sync.event.ChangeEvent;
import de.qabel.desktop.daemon.sync.event.LocalDeleteEvent;
import de.qabel.desktop.daemon.sync.event.RemoteChangeEvent;
import de.qabel.desktop.daemon.sync.event.WatchEvent;
import de.qabel.desktop.daemon.sync.worker.index.SyncIndex;
import de.qabel.desktop.exceptions.QblStorageException;
import de.qabel.desktop.storage.BoxNavigation;
import de.qabel.desktop.storage.cache.CachedBoxVolume;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.TimeUnit;

public class DefaultSyncer implements Syncer {
	private BoxSyncBasedUploadFactory uploadFactory = new BoxSyncBasedUploadFactory();
	private final Logger logger = LoggerFactory.getLogger(getClass());
	private CachedBoxVolume boxVolume;
	private BoxSyncConfig config;
	private TransferManager manager;
	private int pollInterval = 2;
	private TimeUnit pollUnit = TimeUnit.SECONDS;
	private Thread poller;
	private TreeWatcher watcher;
	private boolean polling = false;
	private final SyncIndex index;

	public DefaultSyncer(BoxSyncConfig config, CachedBoxVolume boxVolume, TransferManager manager) {
		this.config = config;
		this.boxVolume = boxVolume;
		this.manager = manager;
		index = config.getSyncIndex();
	}

	@Override
	public void run() {
		startWatcher();
		registerRemoteChangeHandler();
		registerRemotePoller();

		try {
			boxVolume.navigate().notifyAllContents();
		} catch (QblStorageException e) {
			logger.warn(e.getMessage(), e);
		}
	}

	protected void registerRemotePoller() {
		poller = new Thread(() -> {
			try {
				while (!Thread.interrupted()) {
					try {
						boxVolume.navigate().refresh();
						polling = true;
					} catch (QblStorageException e) {
						e.printStackTrace();
					}
					Thread.sleep(pollUnit.toMillis(pollInterval));
				}
			} catch (InterruptedException e) {
				logger.debug("poller stopped");
			} finally {
				polling = false;
			}
		});
		poller.setDaemon(true);
		poller.start();
	}

	protected void registerRemoteChangeHandler() {
		try {
			if (boxVolume.navigate() != null) {
				boxVolume.navigate().addObserver((o, arg) -> {
					if (!(arg instanceof ChangeEvent)) {
						return;
					}
					String type =((ChangeEvent)arg).getType().toString();
					logger.trace("remote update " + type + " " + ((ChangeEvent)arg).getPath().toString());
					download((ChangeEvent) arg);
				});
			}
		} catch (QblStorageException e) {
			throw new IllegalStateException("Failed to watch remote dir: " + e.getMessage(), e);
		}
	}

	private void download(ChangeEvent event) {
		BoxSyncBasedDownload download = new BoxSyncBasedDownload(boxVolume, config, event);
		if (download.getType() != Transaction.TYPE.DELETE && isUpToDate(download)) {
			uploadUnnoticedDelete(download);
			return;
		}
		download.onSuccess(() -> index.update(download.getDestination(), download.getMtime(), download.getType() != Transaction.TYPE.DELETE));
		manager.addDownload(download);
	}

	private boolean isUpToDate(BoxSyncBasedDownload download) {
		return index.isUpToDate(download.getDestination(), download.getMtime(), true);
	}

	private void uploadUnnoticedDelete(BoxSyncBasedDownload download) {
		ChangeEvent inverseEvent = new LocalDeleteEvent(
				download.getDestination(),
				download.isDir(),
				System.currentTimeMillis(),
				ChangeEvent.TYPE.DELETE
		);
		upload(inverseEvent);
	}

	private void upload(WatchEvent event) {
		BoxSyncBasedUpload upload = uploadFactory.getUpload(boxVolume, config, event);
		if (isUpToDate(upload)) {
			downloadUnnoticedDelete(upload);
			return;
		}
		upload.onSuccess(() -> {
			index.update(upload.getSource(), upload.getMtime(), upload.getType() != Transaction.TYPE.DELETE);
		});
		manager.addUpload(upload);
	}

	private boolean isUpToDate(BoxSyncBasedUpload upload) {
		return index.isUpToDate(upload.getSource(), upload.getMtime(), upload.getType() != Transaction.TYPE.DELETE);
	}

	private void downloadUnnoticedDelete(BoxSyncBasedUpload upload) {
		Path destination = upload.getDestination();
		boolean exists;
		BoxNavigation nav = null;
		try {
			nav = upload.getBoxVolume().navigate();
			for (int i = 0; i < destination.getNameCount() - 1; i++) {
				nav = nav.navigate(destination.getName(i).toString());
			}
			String filename = destination.getFileName().toString();
			exists = upload.isDir() ? nav.hasFolder(filename) : nav.hasFile(filename);
		} catch (QblStorageException | IllegalArgumentException e) {
			logger.warn(e.getMessage(), e);
			exists = false;
		}
		if (!exists) {
			ChangeEvent inverseEvent = new RemoteChangeEvent(
					upload.getDestination(),
					upload.isDir(),
					System.currentTimeMillis(),
					ChangeEvent.TYPE.DELETE,
					null, nav
			);
			download(inverseEvent);
		}
	}

	protected void startWatcher() {
		Path localPath = config.getLocalPath();
		if (!Files.isDirectory(localPath) || !Files.isReadable(localPath)) {
			throw new IllegalStateException("local dir " + localPath.toString() + " is not valid");
		}
		watcher = new TreeWatcher(localPath, watchEvent -> {
			if (!watchEvent.isValid()) {
				return;
			}
			String type = "";
			if (watchEvent instanceof ChangeEvent)
				type = ((ChangeEvent)watchEvent).getType().toString();
			logger.trace("local update " + type + " " + watchEvent.getPath().toString());
			upload(watchEvent);
		});
		watcher.setDaemon(true);
		watcher.start();
	}

	@Override
	public void shutdown() {
		if (watcher != null && watcher.isAlive()) {
			watcher.interrupt();
		}
		if (poller != null && poller.isAlive()) {
			poller.interrupt();
		}
	}

	@Override
	public void setPollInterval(int amount, TimeUnit unit) {
		pollInterval = amount;
		pollUnit = unit;
	}

	public boolean isPolling() {
		return polling;
	}

	public void waitFor() {
		while (!polling && !watcher.isWatching()) {
			Thread.yield();
		}
	}

	public BoxSyncBasedUploadFactory getUploadFactory() {
		return uploadFactory;
	}

	public void setUploadFactory(BoxSyncBasedUploadFactory uploadFactory) {
		this.uploadFactory = uploadFactory;
	}
}
