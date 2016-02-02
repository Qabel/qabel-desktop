package de.qabel.desktop.daemon.management;

import de.qabel.desktop.storage.ProgressListener;

public class TransactionRelatedProgressListener extends ProgressListener {
	private Transaction transaction;

	public TransactionRelatedProgressListener(Transaction transaction) {
		this.transaction = transaction;
	}

	@Override
	public void setProgress(long progress) {
		transaction.setProgress(progress);
	}

	@Override
	public void setSize(long size) {
		transaction.setSize(size);
	}
}
