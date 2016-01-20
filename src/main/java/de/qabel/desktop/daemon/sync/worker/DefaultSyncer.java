package de.qabel.desktop.daemon.sync.worker;

import de.qabel.desktop.config.BoxSyncConfig;
import de.qabel.desktop.daemon.management.BoxSyncBasedUpload;
import de.qabel.desktop.daemon.management.LoadManager;
import de.qabel.desktop.storage.BoxVolume;

public class DefaultSyncer implements Syncer {
	private BoxVolume boxVolume;
	private BoxSyncConfig config;
	private LoadManager manager;

	public DefaultSyncer(BoxSyncConfig config, BoxVolume boxVolume, LoadManager manager) {
		this.config = config;
		this.boxVolume = boxVolume;
		this.manager = manager;
	}

	@Override
	public void run() {
		TreeWatcher watcher = new TreeWatcher(config.getLocalPath(), watchEvent -> {
			if (!watchEvent.isValid()) {
				return;
			}
			manager.addUpload(new BoxSyncBasedUpload(boxVolume, config, watchEvent));
		});
		watcher.start();
	}
}
