package de.qabel.desktop.daemon.sync.worker;

import de.qabel.desktop.config.BoxSyncConfig;

public class FakeSyncer implements Syncer {
	public BoxSyncConfig config;
	public boolean started;

	public FakeSyncer(BoxSyncConfig config) {
		this.config = config;
	}

	@Override
	public void run() {
		started = true;
	}
}
