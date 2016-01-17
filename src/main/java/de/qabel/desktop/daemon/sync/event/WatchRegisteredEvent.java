package de.qabel.desktop.daemon.sync.event;

import java.nio.file.Path;

public class WatchRegisteredEvent extends AbstractChangeEvent {
	public WatchRegisteredEvent(Path path) {
		super(path, ChangeEvent.TYPE.CREATE);
	}
}
