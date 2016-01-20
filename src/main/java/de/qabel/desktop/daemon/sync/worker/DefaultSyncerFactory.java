package de.qabel.desktop.daemon.sync.worker;

import de.qabel.desktop.config.BoxSyncConfig;
import de.qabel.desktop.config.factory.BoxVolumeFactory;
import de.qabel.desktop.daemon.management.LoadManager;

public class DefaultSyncerFactory implements SyncerFactory {
	private LoadManager manager;

	private BoxVolumeFactory boxVolumeFactory;

	public DefaultSyncerFactory(BoxVolumeFactory boxVolumeFactory, LoadManager manager) {
		this.boxVolumeFactory = boxVolumeFactory;
		this.manager = manager;
	}

	@Override
	public Syncer factory(BoxSyncConfig config) {
		return new DefaultSyncer(config, boxVolumeFactory.getVolume(config.getAccount(), config.getIdentity()), manager);
	}
}
