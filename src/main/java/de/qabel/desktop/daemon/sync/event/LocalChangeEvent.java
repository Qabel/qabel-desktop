package de.qabel.desktop.daemon.sync.event;

import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class LocalChangeEvent extends AbstractChangeEvent implements ChangeEvent {
	public LocalChangeEvent(Path path, ChangeEvent.TYPE type) throws IOException {
		this(path, Files.isDirectory(path), Files.getLastModifiedTime(path).toMillis(), type);
	}

	public LocalChangeEvent(Path path, boolean isDir, Long mtime, ChangeEvent.TYPE type) {
		super(path, isDir, mtime, type);
	}

	@Override
	public boolean isValid() {
		long currentMtime = getPath().toFile().lastModified();
		boolean valid = currentMtime == getMtime();
		if (!valid) {
			LoggerFactory.getLogger(getClass()).debug("event invalid: mtime " + currentMtime + " != " + getMtime() + " on " + getPath().toString());
		}
		return valid;
	}
}
