package de.qabel.desktop.daemon.sync.event;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class LocalChangeEvent extends AbstractChangeEvent {
	public LocalChangeEvent(Path path, ChangeEvent.TYPE type) throws IOException {
		super(path, Files.isDirectory(path), Files.getLastModifiedTime(path).toMillis(), type);
	}
}
