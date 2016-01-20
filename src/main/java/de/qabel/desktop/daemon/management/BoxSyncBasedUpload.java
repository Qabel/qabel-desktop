package de.qabel.desktop.daemon.management;

import de.qabel.desktop.config.BoxSyncConfig;
import de.qabel.desktop.daemon.sync.event.WatchEvent;
import de.qabel.desktop.storage.BoxVolume;

import java.nio.file.Path;

public class BoxSyncBasedUpload implements Upload {
	private final BoxSyncConfig boxSyncConfig;
	private final WatchEvent event;
	private final BoxVolume volume;

	public BoxSyncBasedUpload(BoxVolume volume, BoxSyncConfig boxSyncConfig,  WatchEvent event) {
		this.event = event;
		this.volume = volume;
		this.boxSyncConfig = boxSyncConfig;
	}

	@Override
	public TYPE getType() {
		return null;
	}

	@Override
	public BoxVolume getBoxVolume() {
		return volume;
	}

	@Override
	public Path getSource() {
		return event.getPath();
	}

	@Override
	public Path getDestination() {
		Path relativePath = boxSyncConfig.getLocalPath().relativize(event.getPath());
		return boxSyncConfig.getRemotePath().resolve(relativePath);
	}

	@Override
	public boolean isValid() {
		return event.isValid();
	}

	@Override
	public String toString() {
		return "Upload[" + getSource() + " to " + getDestination() + "]";
	}
}
