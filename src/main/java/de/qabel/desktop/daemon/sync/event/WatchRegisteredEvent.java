package de.qabel.desktop.daemon.sync.event;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class WatchRegisteredEvent extends LocalChangeEvent {
	public WatchRegisteredEvent(Path path) throws IOException {
		super(path, ChangeEvent.TYPE.CREATE);
	}
}
