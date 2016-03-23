package de.qabel.desktop.daemon.sync.worker;

import de.qabel.desktop.config.BoxSyncConfig;

import java.util.concurrent.TimeUnit;

public class FakeSyncer implements Syncer {
	public BoxSyncConfig config;
	public boolean started;
	public boolean stopped;

	public FakeSyncer(BoxSyncConfig config) {
		this.config = config;
	}

	@Override
	public void run() {
		started = true;
	}

	@Override
	public void shutdown() {

	}

	@Override
	public void setPollInterval(int amount, TimeUnit unit) {

	}

	@Override
	public void stop() throws InterruptedException {
		stopped = true;
	}
}
