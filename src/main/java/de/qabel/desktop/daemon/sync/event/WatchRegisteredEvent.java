package de.qabel.desktop.daemon.sync.event;

import java.nio.file.Path;

public class WatchRegisteredEvent extends AbstractWatchEvent implements WatchEvent {
	public WatchRegisteredEvent(Path path) {
		super(path);
	}
}
