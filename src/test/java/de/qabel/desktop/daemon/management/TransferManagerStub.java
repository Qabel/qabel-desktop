package de.qabel.desktop.daemon.management;

import java.util.LinkedList;
import java.util.List;

public class TransferManagerStub implements TransferManager {
	private List<Transaction> transactions = new LinkedList<>();
	public boolean hasRun;

	@Override
	public List<Transaction> getTransactions() {
		return transactions;
	}

	@Override
	public void addUpload(Upload upload) {
		transactions.add(upload);
	}

	@Override
	public void addDownload(Download download) {
		transactions.add(download);
	}

	@Override
	public List<Transaction> getHistory() {
		return transactions;
	}

	@Override
	public void cleanup() {

	}

	@Override
	public void run() {
		hasRun = true;
	}
}
