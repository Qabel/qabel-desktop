package de.qabel.desktop.daemon.management;

import java.util.List;
import java.util.concurrent.TimeUnit;

public interface TransferManager extends Runnable {
    List<Transaction> getTransactions();
    void addUpload(Upload upload);
    void addDownload(Download download);
    List<Transaction> getHistory();

    /**
     * removes Transactions that are no longer valid or cancelled
     */
    void cleanup();
}
