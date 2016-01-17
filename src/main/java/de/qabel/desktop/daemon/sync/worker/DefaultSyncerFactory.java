package de.qabel.desktop.daemon.sync.worker;

import de.qabel.desktop.config.BoxSyncConfig;
import de.qabel.desktop.daemon.management.LoadManager;

public class DefaultSyncerFactory implements SyncerFactory {
	private LoadManager manager;

	public DefaultSyncerFactory(LoadManager manager) {
		this.manager = manager;
	}

	@Override
	public Syncer factory(BoxSyncConfig config) {
		return new DefaultSyncer(config, manager);
	}
}
