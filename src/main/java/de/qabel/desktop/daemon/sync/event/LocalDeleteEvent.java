package de.qabel.desktop.daemon.sync.event;

import java.nio.file.Files;
import java.nio.file.Path;

public class LocalDeleteEvent extends AbstractChangeEvent implements ChangeEvent {
	public LocalDeleteEvent(Path path, boolean isDirecotry, Long mtime, TYPE type) {
		super(path, isDirecotry, mtime, type);
	}

	@Override
	public boolean isValid() {
		return !Files.exists(getPath());
	}
}
