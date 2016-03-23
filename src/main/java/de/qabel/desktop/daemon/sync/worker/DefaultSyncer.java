package de.qabel.desktop.daemon.sync.worker;

import de.qabel.desktop.daemon.management.BoxSyncBasedUpload;
import de.qabel.desktop.daemon.management.TransferManager;

import de.qabel.desktop.config.BoxSyncConfig;
import de.qabel.desktop.daemon.management.*;
import de.qabel.desktop.daemon.sync.BoxSync;
import de.qabel.desktop.daemon.sync.blacklist.Blacklist;
import de.qabel.desktop.daemon.sync.event.ChangeEvent;
import de.qabel.desktop.daemon.sync.event.LocalDeleteEvent;
import de.qabel.desktop.daemon.sync.event.RemoteChangeEvent;
import de.qabel.desktop.daemon.sync.event.WatchEvent;
import de.qabel.desktop.daemon.sync.worker.index.SyncIndex;
import de.qabel.desktop.daemon.sync.worker.index.SyncIndexEntry;
import de.qabel.desktop.exceptions.QblStorageException;
import de.qabel.desktop.storage.BoxNavigation;
import de.qabel.desktop.storage.cache.CachedBoxNavigation;
import de.qabel.desktop.storage.cache.CachedBoxVolume;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Observer;
import java.util.concurrent.TimeUnit;

import static de.qabel.desktop.daemon.sync.event.ChangeEvent.TYPE.UPDATE;

public class DefaultSyncer implements Syncer, BoxSync, HasProgress {
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
	private WindowedTransactionGroup progress = new WindowedTransactionGroup();
	private Blacklist localBlacklist;
	private Observer remoteChangeHandler;

	public DefaultSyncer(BoxSyncConfig config, CachedBoxVolume boxVolume, TransferManager manager) {
		this.config = config;
		this.boxVolume = boxVolume;
		this.manager = manager;
		index = config.getSyncIndex();
		config.setSyncer(this);
	}

	public void setLocalBlacklist(Blacklist blacklist) {
		this.localBlacklist = blacklist;
	}

	@Override
	public void run() {
		try {
			startWatcher();
			registerRemoteChangeHandler();
			registerRemotePoller();

			boxVolume.navigate().notifyAllContents();
		} catch (QblStorageException | IOException e) {
			logger.error("failed to start sync: " + e.getMessage(), e);
		}
	}

	protected void registerRemotePoller() throws QblStorageException {
		CachedBoxNavigation nav = navigateToRemoteDir();

		poller = new Thread(() -> {
			try {
				while (!Thread.interrupted()) {
					try {
						nav.refresh();
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
		poller.setName("DefaultSyncer-" + config.getName() + "#Poller");
		poller.setDaemon(true);
		poller.start();
	}

	protected void registerRemoteChangeHandler() throws QblStorageException {
		CachedBoxNavigation nav = navigateToRemoteDir();

		remoteChangeHandler = (o, arg) -> {
			try {
				if (!(arg instanceof ChangeEvent)) {
					return;
				}
				String type = ((ChangeEvent) arg).getType().toString();
				logger.trace("remote update " + type + " " + ((ChangeEvent) arg).getPath().toString());
				download((ChangeEvent) arg);
			} catch (Exception e) {
				logger.error("failed to handle remote change: " + e.getMessage(), e);
			}
		};
		nav.addObserver(remoteChangeHandler);
	}

	private CachedBoxNavigation navigateToRemoteDir() throws QblStorageException {
		Path remotePath = config.getRemotePath();
		CachedBoxNavigation nav = boxVolume.navigate();

		for (int i = 0; i < remotePath.getNameCount(); i++) {
			String name = remotePath.getName(i).toString();
			if (!nav.hasFolder(name)) {
				index.clear();
				nav.createFolder(name);
			}
			nav = nav.navigate(name);
		}
		return nav;
	}

	private void download(ChangeEvent event) {
		BoxSyncBasedDownload download = new BoxSyncBasedDownload(boxVolume, config, event);

		if (!download.getDestination().normalize().startsWith(config.getLocalPath().normalize())) {
			logger.warn("syncer received event from outside sync path: " + download);
			return;
		}

		if (download.getType() != Transaction.TYPE.DELETE && isUpToDate(download)) {
			uploadUnnoticedDelete(download);
			return;
		}

		SyncIndexEntry entry = index.get(download.getDestination());
		if (entry != null && download.getMtime() != null && entry.getLocalMtime() >= download.getMtime()) {
			return;
		}

		download.onSuccess(() -> index.update(download.getDestination(), download.getMtime(), download.getType() != Transaction.TYPE.DELETE));
		addDownload(download);
	}

	private void addDownload(BoxSyncBasedDownload download) {
		progress.add(download);
		manager.addDownload(download);
	}

	private boolean isUpToDate(BoxSyncBasedDownload download) {
		return index.isUpToDate(download.getDestination(), download.getMtime(), true);
	}

	private void uploadUnnoticedDelete(BoxSyncBasedDownload download) {
		if (Files.exists(download.getDestination())) {
			return;
		}
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

		if (localBlacklist != null) {
			if (localBlacklist.matches(upload.getDestination())) {
				return;
			}
		}

		if (isUpToDate(upload)) {
			downloadUnnoticedDelete(upload);
			return;
		}
		SyncIndexEntry entry = index.get(upload.getSource());
		if (entry != null && entry.isExisting() == (upload.getType() != Transaction.TYPE.DELETE) && entry.getLocalMtime() >= upload.getMtime()) {
			return;
		}
		if (event.isDir() && event instanceof ChangeEvent && ((ChangeEvent)event).getType() == UPDATE) {
			return;
		}

		upload.onSuccess(() -> {
			index.update(upload.getSource(), upload.getMtime(), upload.getType() != Transaction.TYPE.DELETE);
		});
		addUpload(upload);
	}

	private void addUpload(BoxSyncBasedUpload upload) {
		progress.add(upload);
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

	private int localEvents = 0;

	public boolean isProcessingLocalEvents() {
		return localEvents > 0;
	}

	protected void startWatcher() throws IOException {
		Path localPath = config.getLocalPath();
		if (!Files.isDirectory(localPath)) {
			Files.createDirectories(localPath);
		}
		if (!Files.isReadable(localPath)) {
			throw new IllegalStateException("local dir " + localPath.toString() + " is not valid");
		}
		watcher = new TreeWatcher(localPath, watchEvent -> {
			try {
				synchronized (DefaultSyncer.this) {
					localEvents++;
				}
				if (!watchEvent.isValid()) {
					return;
				}
				String type = "";
				if (watchEvent instanceof ChangeEvent)
					type = ((ChangeEvent) watchEvent).getType().toString();
				logger.trace("local update " + type + " " + watchEvent.getPath().toString());
				upload(watchEvent);
			} finally {
				synchronized (DefaultSyncer.this) {
					localEvents--;
				}
			}
		});
		watcher.setName("DefaultSyncer-" + config.getName() + "#Watcher");
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

	public void join() throws InterruptedException {
		watcher.join();
		poller.join();
	}

	@Override
	public void setPollInterval(int amount, TimeUnit unit) {
		pollInterval = amount;
		pollUnit = unit;
	}

	@Override
	public void stop() throws InterruptedException {
		watcher.interrupt();
		watcher.join();
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

	@Override
	public boolean isSynced() {
		return progress.isEmpty();
	}

	@Override
	public double getProgress() {
		return progress.getProgress();
	}

	@Override
	public Object onProgress(Runnable runnable) {
		return progress.onProgress(runnable);
	}

	@Override
	public int countFiles() {
		return 0;
	}

	@Override
	public int countFolders() {
		return 0;
	}

	@Override
	public boolean hasError() {
		return false;
	}
}
