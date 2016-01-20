package de.qabel.desktop.daemon.management;

import de.qabel.desktop.config.BoxSyncConfig;
import de.qabel.desktop.daemon.sync.event.WatchEvent;
import de.qabel.desktop.storage.BoxVolume;

import java.nio.file.Path;

public class BoxSyncBasedDownload extends AbstractBoxSyncBasedTransaction implements Download {
	public BoxSyncBasedDownload(BoxVolume volume, BoxSyncConfig boxSyncConfig,  WatchEvent event) {
		super(volume, event, boxSyncConfig);
	}

	@Override
	public Path getDestination() {
		Path relativePath = boxSyncConfig.getRemotePath().relativize(getSource());
		return boxSyncConfig.getLocalPath().resolve(relativePath);
	}

	@Override
	public boolean isValid() {
		return event.isValid();
	}

	@Override
	public String toString() {
		return "Download[" + getSource() + " to " + getDestination() + "]";
	}
}
