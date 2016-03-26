package de.qabel.desktop.daemon.sync.worker;

import de.qabel.desktop.daemon.management.HasProgressCollection;
import de.qabel.desktop.daemon.management.Transaction;

import java.util.List;
import java.util.concurrent.TimeUnit;

public interface Syncer extends Runnable, HasProgressCollection<Syncer, Transaction> {
	List<Transaction> getHistory();

	void shutdown();
	void setPollInterval(int amount, TimeUnit unit);

	void stop() throws InterruptedException;
}
