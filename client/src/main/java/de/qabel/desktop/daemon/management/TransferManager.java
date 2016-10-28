package de.qabel.desktop.daemon.management;

import java.util.List;

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
