package de.qabel.desktop.daemon.sync.event;

import java.nio.file.Path;

public class DefaultChangeEvent extends AbstractChangeEvent implements ChangeEvent {
	public DefaultChangeEvent(Path path, boolean isDirectory, Long mtime, TYPE type) {
		super(path, isDirectory, mtime, type);
	}
}
