package de.qabel.desktop.daemon.sync.event;

import java.nio.file.Path;

public interface WatchEvent {
	Path getPath();

	boolean isValid();
}
