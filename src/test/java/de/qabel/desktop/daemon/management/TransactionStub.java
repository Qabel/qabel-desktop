package de.qabel.desktop.daemon.management;

import de.qabel.desktop.storage.BoxVolume;

import java.nio.file.Path;

public class TransactionStub implements Transaction {
	public BoxVolume volume;
	public Path source;
	public Path destination;
	public boolean valid = true;
	public TYPE type = TYPE.CREATE;
	public boolean isDir = true;
	public STATE state = STATE.INITIALIZING;
	public Long mtime = 0L;
	public boolean closed = false;
	public long transactionAge = 2000L;
	public long stagingDelay = 0L;

	@Override
	public long transactionAge() {
		return transactionAge;
	}

	@Override
	public TYPE getType() {
		return type;
	}

	@Override
	public BoxVolume getBoxVolume() {
		return volume;
	}

	@Override
	public Path getSource() {
		return source;
	}

	@Override
	public Path getDestination() {
		return destination;
	}

	@Override
	public boolean isValid() {
		return valid;
	}

	@Override
	public boolean isDir() {
		return isDir;
	}

	@Override
	public Long getMtime() {
		return mtime;
	}

	@Override
	public Transaction onSuccess(Runnable runnable) {
		return this;
	}

	@Override
	public Transaction onFailure(Runnable runnable) {
		return this;
	}

	@Override
	public Transaction onSkipped(Runnable runnable) {
		return this;
	}

	@Override
	public long getStagingDelayMillis() {
		return stagingDelay;
	}

	@Override
	public STATE getState() {
		return state;
	}

	@Override
	public void toState(STATE state) {
		this.state = state;
	}

	@Override
	public void close() {
		closed = true;
	}
}
