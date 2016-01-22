package de.qabel.desktop.daemon.management;

import de.qabel.desktop.storage.BoxVolume;

import java.nio.file.Path;

public class DummyUpload implements Upload {
	@Override
	public TYPE getType() {
		return null;
	}

	@Override
	public BoxVolume getBoxVolume() {
		return null;
	}

	@Override
	public Path getSource() {
		return null;
	}

	@Override
	public Path getDestination() {
		return null;
	}

	@Override
	public boolean isValid() {
		return false;
	}

	@Override
	public boolean isDir() {
		return false;
	}

	@Override
	public Long getMtime() {
		return null;
	}

	@Override
	public STATE getState() {
		return null;
	}

	@Override
	public void toState(STATE state) {

	}
}
