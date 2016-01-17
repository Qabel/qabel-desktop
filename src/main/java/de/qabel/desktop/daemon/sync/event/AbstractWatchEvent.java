package de.qabel.desktop.daemon.sync.event;

import java.nio.file.Path;

public class AbstractWatchEvent implements WatchEvent {
	private final Path path;

	public AbstractWatchEvent(Path path) {
		this.path = path;
	}

	@Override
	public Path getPath() {
		return path;
	}
}
