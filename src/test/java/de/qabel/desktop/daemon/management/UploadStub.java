package de.qabel.desktop.daemon.management;

import de.qabel.desktop.storage.BoxVolume;

import java.nio.file.Path;

public class UploadStub implements Upload {
	public BoxVolume volume;
	public Path source;
	public Path destination;
	public boolean valid = true;
	public TYPE type = TYPE.CREATE;

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
}
