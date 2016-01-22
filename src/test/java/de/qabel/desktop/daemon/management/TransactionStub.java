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
	public STATE getState() {
		return state;
	}

	@Override
	public void toState(STATE state) {
		this.state = state;
	}
}
