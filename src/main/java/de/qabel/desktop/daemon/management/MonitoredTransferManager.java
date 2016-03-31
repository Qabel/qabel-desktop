package de.qabel.desktop.daemon.management;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;

public class MonitoredTransferManager implements TransferManager {
	private static final ExecutorService executor = Executors.newSingleThreadExecutor();
	private List<Consumer<Transaction>> addListeners = new LinkedList<>();
	private TransferManager manager;

	public MonitoredTransferManager(TransferManager manager) {
		this.manager = manager;
	}

	@Override
	public List<Transaction> getTransactions() {
		return manager.getTransactions();
	}

	@Override
	public void addUpload(Upload upload) {
		notifyListeners(upload);
		manager.addUpload(upload);
	}

	private void notifyListeners(Transaction transaction) {
		executor.submit(() -> addListeners.forEach(c -> c.accept(transaction)));
	}

	@Override
	public void addDownload(Download download) {
		notifyListeners(download);
		manager.addDownload(download);
	}

	@Override
	public List<Transaction> getHistory() {
		return manager.getHistory();
	}

	@Override
	public void cleanup() {
		manager.cleanup();
	}

	@Override
	public void run() {
		manager.run();
	}

	public void onAdd(Consumer<Transaction> consumer) {
		addListeners.add(consumer);
	}
}
