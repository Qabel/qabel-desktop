package de.qabel.desktop.daemon.sync.worker;

import de.qabel.desktop.config.BoxSyncConfig;

public class DefaultSyncerFactory implements SyncerFactory {
	@Override
	public Syncer factory(BoxSyncConfig config) {
		return new DefaultSyncer(config);
	}
}
