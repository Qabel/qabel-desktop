package de.qabel.desktop.daemon.sync.event;

import java.nio.file.Path;

public class RemoteChangeEvent extends AbstractChangeEvent implements ChangeEvent {
	public RemoteChangeEvent(Path path, boolean isDirecotry, Long mtime, ChangeEvent.TYPE type) {
		super(path, isDirecotry, mtime, type);
	}

	@Override
	public boolean isValid() {
		return true;
	}
}
