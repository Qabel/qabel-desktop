package de.qabel.desktop.daemon.sync.worker;

import de.qabel.desktop.config.BoxSyncConfig;
import de.qabel.desktop.daemon.management.BoxSyncBasedUpload;
import de.qabel.desktop.daemon.management.LoadManager;

public class DefaultSyncer implements Syncer {
	private BoxSyncConfig config;
	private LoadManager manager;

	public DefaultSyncer(BoxSyncConfig config, LoadManager manager) {
		this.config = config;
		this.manager = manager;
	}

	@Override
	public void run() {
		TreeWatcher watcher = new TreeWatcher(config.getLocalPath(), watchEvent -> {
			if (!watchEvent.isValid()) {
				return;
			}
			manager.addUpload(new BoxSyncBasedUpload(config, watchEvent));
		});
		watcher.start();
	}
}
