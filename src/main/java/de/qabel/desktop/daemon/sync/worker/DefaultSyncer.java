package de.qabel.desktop.daemon.sync.worker;

import de.qabel.desktop.config.BoxSyncConfig;
import de.qabel.desktop.daemon.management.BoxSyncBasedDownload;
import de.qabel.desktop.daemon.management.BoxSyncBasedUpload;
import de.qabel.desktop.daemon.management.LoadManager;
import de.qabel.desktop.daemon.management.Transaction;
import de.qabel.desktop.daemon.sync.event.ChangeEvent;
import de.qabel.desktop.daemon.sync.event.LocalChangeEvent;
import de.qabel.desktop.daemon.sync.event.LocalDeleteEvent;
import de.qabel.desktop.daemon.sync.event.WatchEvent;
import de.qabel.desktop.daemon.sync.worker.index.SyncIndex;
import de.qabel.desktop.exceptions.QblStorageException;
import de.qabel.desktop.storage.cache.CachedBoxVolume;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.TimeUnit;

public class DefaultSyncer implements Syncer {
	private final Logger logger = LoggerFactory.getLogger(getClass());
	private CachedBoxVolume boxVolume;
	private BoxSyncConfig config;
	private LoadManager manager;
	private int pollInterval = 2;
	private TimeUnit pollUnit = TimeUnit.SECONDS;
	private Thread poller;
	private TreeWatcher watcher;
	private boolean polling = false;
	private final SyncIndex index;

	public DefaultSyncer(BoxSyncConfig config, CachedBoxVolume boxVolume, LoadManager manager) {
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
		if (download.getType() != Transaction.TYPE.DELETE && index.isUpToDate(download.getDestination(), download.getMtime(), true)) {

			ChangeEvent inverseEvent = new LocalDeleteEvent(
					download.getDestination(),
					download.isDir(),
					System.currentTimeMillis(),
					ChangeEvent.TYPE.DELETE
			);
			manager.addUpload(new BoxSyncBasedUpload(boxVolume, config, inverseEvent));
			return;
		}
		download.onSuccess(() -> index.update(download.getDestination(), download.getMtime(), download.getType() != Transaction.TYPE.DELETE));
		manager.addDownload(download);
	}

	private void upload(WatchEvent event) {
		BoxSyncBasedUpload upload = new BoxSyncBasedUpload(boxVolume, config, event);
		if (index.isUpToDate(upload.getSource(), upload.getMtime(), upload.getType() != Transaction.TYPE.DELETE)) {
			return;
		}
		upload.onSuccess(() -> {
			index.update(upload.getSource(), upload.getMtime(), upload.getType() != Transaction.TYPE.DELETE);
		});
		manager.addUpload(upload);
	}

	protected void startWatcher() {
		watcher = new TreeWatcher(config.getLocalPath(), watchEvent -> {
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
}
