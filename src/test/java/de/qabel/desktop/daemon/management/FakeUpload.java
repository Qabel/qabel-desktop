package de.qabel.desktop.daemon.management;

import de.qabel.desktop.storage.BoxVolumeConfig;

import java.nio.file.Path;

public class FakeUpload implements Upload {
	@Override
	public BoxVolumeConfig getBoxVolumeConfig() {
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
}
