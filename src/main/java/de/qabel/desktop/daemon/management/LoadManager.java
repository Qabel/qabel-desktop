package de.qabel.desktop.daemon.management;

import java.util.List;

public interface LoadManager {
	List<Transaction> getTransactions();
	void addUpload(Upload upload);
	void addDownload(Download download);
}
