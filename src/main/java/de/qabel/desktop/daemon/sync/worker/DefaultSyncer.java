package de.qabel.desktop.daemon.sync.worker;

import de.qabel.desktop.config.BoxSyncConfig;
import de.qabel.desktop.daemon.management.BoxSyncBasedDownload;
import de.qabel.desktop.daemon.management.BoxSyncBasedUpload;
import de.qabel.desktop.daemon.management.LoadManager;
import de.qabel.desktop.daemon.sync.event.ChangeEvent;
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
					System.out.println(this.hashCode() + ": remote update " + type + " " + ((ChangeEvent)arg).getPath().toString());
					manager.addDownload(new BoxSyncBasedDownload(boxVolume, config, (ChangeEvent) arg));
				});
			}
		} catch (QblStorageException e) {
			throw new IllegalStateException("Failed to watch remote dir: " + e.getMessage(), e);
		}
	}

	protected void startWatcher() {
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
