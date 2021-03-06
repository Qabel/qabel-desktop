package de.qabel.desktop.daemon.sync.worker;

import de.qabel.desktop.daemon.management.HasProgressCollection;
import de.qabel.desktop.daemon.management.Transaction;
import de.qabel.desktop.daemon.sync.BoxSync;

import java.util.List;
import java.util.concurrent.TimeUnit;

public interface Syncer extends Runnable, HasProgressCollection<Syncer, Transaction>, BoxSync {
    List<Transaction> getHistory();

    /**
     * initiate syncer stop
     */
    void shutdown();
    void setPollInterval(int amount, TimeUnit unit);

    /**
     * stop syncer and await termination
     */
    void stop() throws InterruptedException;
}
