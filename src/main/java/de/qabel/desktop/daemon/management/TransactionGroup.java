package de.qabel.desktop.daemon.management;

import java.util.*;

public class TransactionGroup extends Observable implements Observer, HasProgress<TransactionGroup> {
	public static final long METADATA_SIZE = 56320L;
	protected Set<Transaction> transactions = new HashSet<>();

	public boolean isEmpty() {
		return transactions.isEmpty();
	}

	public void add(Transaction transaction) {
		synchronized (transactions) {
			transactions.add(transaction);
		}
		transaction.onProgress(() -> notify(transaction));
		setChanged();
		notify(transaction);
	}

	public int size() {
		return transactions.size();
	}

	@Override
	public double getProgress() {
		long size = 0;
		long transferred = 0;
		synchronized (transactions) {
			for (Transaction t : transactions.toArray(new Transaction[0])) {
				size += t.hasSize() ? t.getSize() : METADATA_SIZE;
				transferred += t.isDone() ? t.getSize() : t.getTransferred();
			}
		}
		return size == 0 ? 1.0 : (double) transferred / (double) size;
	}

	@Override
	public TransactionGroup onProgress(Runnable runnable) {
		addObserver((o, arg) -> runnable.run());
		return this;
	}

	@Override
	public void update(Observable o, Object arg) {
		setChanged();
		notifyObservers(o);
	}

	private void notify(Transaction transaction) {
		setChanged();
		notifyObservers(transaction);
	}

	public void cancel() {
		synchronized (transactions) {
			for (Transaction t: transactions.toArray(new Transaction[0])) {
				if (t.isDone()) {
					continue;
				}
				t.toState(Transaction.STATE.FAILED);
			}
		}
	}
}
