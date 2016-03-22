package de.qabel.desktop.daemon.sync.worker;

import java.util.concurrent.TimeUnit;

public interface Syncer extends Runnable {
	void shutdown();
	void setPollInterval(int amount, TimeUnit unit);

	void stop() throws InterruptedException;
}
