package de.qabel.desktop.daemon.management;

import java.util.List;
import java.util.concurrent.TimeUnit;

public interface LoadManager extends Runnable {
	List<Transaction> getTransactions();
	void addUpload(Upload upload);
	void addDownload(Download download);

	List<Transaction> getHistory();

	void setStagingDelay(long amount, TimeUnit unit);
}
