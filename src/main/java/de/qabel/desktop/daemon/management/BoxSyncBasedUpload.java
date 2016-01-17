package de.qabel.desktop.daemon.management;

import de.qabel.desktop.config.BoxSyncConfig;
import de.qabel.desktop.daemon.sync.event.WatchEvent;
import de.qabel.desktop.storage.BoxVolumeConfig;

import java.nio.file.Path;

public class BoxSyncBasedUpload implements Upload {
	private final BoxSyncConfig boxSyncConfig;
	private final WatchEvent event;
	private final BoxVolumeConfig boxVolumeConfig;

	public BoxSyncBasedUpload(BoxSyncConfig boxSyncConfig, WatchEvent event) {
		this.boxSyncConfig = boxSyncConfig;
		this.event = event;
		this.boxVolumeConfig = new BoxSyncBasedVolumeConfig(boxSyncConfig);
	}

	@Override
	public BoxVolumeConfig getBoxVolumeConfig() {
		return boxVolumeConfig;
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
		return false;
	}
}
