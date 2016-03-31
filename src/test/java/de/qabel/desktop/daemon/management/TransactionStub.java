package de.qabel.desktop.daemon.management;

import de.qabel.desktop.storage.BoxVolume;

import java.nio.file.Path;

public class TransactionStub extends AbstractTransaction {
	public BoxVolume volume;
	public Path source;
	public Path destination;
	public boolean valid = true;
	public TYPE type = TYPE.CREATE;
	public boolean isDir = true;
	public STATE state = STATE.INITIALIZING;
	public Long mtime = 0L;
	public boolean closed;
	public long transactionAge = 2000L;
	public Long size = 0L;
	public long progress;
	public long stagingDelay;

	public TransactionStub() {
		super(0L);
	}

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
		super.toState(state);
	}

	@Override
	public void close() {
		closed = true;
	}

	@Override
	public long getSize() {
		return size;
	}

	@Override
	public boolean hasSize() {
		return size != null;
	}

	@Override
	public long getTransferred() {
		return progress;
	}

	@Override
	public void setTransferred(long progress) {
		this.progress = progress;
		super.setTransferred(progress);
	}

	@Override
	public void setSize(long size) {
		this.size = size;
	}
}
