package de.qabel.desktop.daemon.sync.event;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class WatchRegisteredEvent extends AbstractWatchEvent {
	public WatchRegisteredEvent(Path path) throws IOException {
		super(path, Files.isDirectory(path), path.toFile().lastModified());
	}

	@Override
	public boolean isValid() {
		return getPath().toFile().exists() && getPath().toFile().lastModified() == getMtime();
	}
}
