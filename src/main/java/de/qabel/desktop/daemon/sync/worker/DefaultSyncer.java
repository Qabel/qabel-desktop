package de.qabel.desktop.daemon.sync.worker;

import de.qabel.desktop.config.BoxSyncConfig;
import de.qabel.desktop.daemon.management.BoxSyncBasedDownload;
import de.qabel.desktop.daemon.management.BoxSyncBasedUpload;
import de.qabel.desktop.daemon.management.LoadManager;
import de.qabel.desktop.daemon.sync.event.ChangeEvent;
import de.qabel.desktop.daemon.sync.event.WatchEvent;
import de.qabel.desktop.exceptions.QblStorageException;
import de.qabel.desktop.storage.BoxVolume;
import de.qabel.desktop.storage.cache.CachedBoxVolume;
import org.apache.log4j.spi.LoggerFactory;

import java.util.concurrent.TimeUnit;

public class DefaultSyncer implements Syncer {
	private CachedBoxVolume boxVolume;
	private BoxSyncConfig config;
	private LoadManager manager;
	private int pollInterval = 5;
	private TimeUnit pollUnit = TimeUnit.SECONDS;
	private Thread poller;
	private TreeWatcher watcher;
	private boolean polling = false;

	public DefaultSyncer(BoxSyncConfig config, CachedBoxVolume boxVolume, LoadManager manager) {
		this.config = config;
		this.boxVolume = boxVolume;
		this.manager = manager;
	}

	@Override
	public void run() {
		watcher = new TreeWatcher(config.getLocalPath(), watchEvent -> {
			if (!watchEvent.isValid()) {
				return;
			}
			String type = "";
			if (watchEvent instanceof ChangeEvent)
				type = ((ChangeEvent)watchEvent).getType().toString();
			System.out.println(this.hashCode() + ": local update " + type + " " + watchEvent.getPath().toString());
			manager.addUpload(new BoxSyncBasedUpload(boxVolume, config, watchEvent));
		});
		watcher.setDaemon(true);
		watcher.start();

		try {
			if (boxVolume.navigate() != null) {
				boxVolume.navigate().addObserver((o, arg) -> {
					if (!(arg instanceof ChangeEvent)) {
						return;
					}
					String type =((ChangeEvent)arg).getType().toString();
					System.out.println(this.hashCode() + ": remote update " + type + " " + ((ChangeEvent)arg).getPath().toString());
					manager.addDownload(new BoxSyncBasedDownload(boxVolume, config, (ChangeEvent) arg));
				});
			}
		} catch (QblStorageException e) {
			throw new IllegalStateException("Failed to watch remote dir: " + e.getMessage(), e);
		}

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
				org.slf4j.LoggerFactory.getLogger(getClass()).debug("poller stopped");
			} finally {
				polling = false;
			}
		});
		poller.setDaemon(true);
		poller.start();
		try {
			boxVolume.navigate().notifyAllContents();
		} catch (QblStorageException e) {
			org.slf4j.LoggerFactory.getLogger(getClass()).warn(e.getMessage(), e);
		}
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
