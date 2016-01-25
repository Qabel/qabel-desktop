package de.qabel.desktop.daemon.management;

import de.qabel.desktop.config.BoxSyncConfig;
import de.qabel.desktop.daemon.sync.event.WatchEvent;
import de.qabel.desktop.storage.BoxVolume;

import java.nio.file.Path;

public class BoxSyncBasedUpload extends AbstractBoxSyncBasedTransaction implements Upload {
	public BoxSyncBasedUpload(BoxVolume volume, BoxSyncConfig boxSyncConfig,  WatchEvent event) {
		super(volume, event, boxSyncConfig);
	}

	@Override
	public Path getDestination() {
		Path relativePath = boxSyncConfig.getLocalPath().relativize(getSource());
		return boxSyncConfig.getRemotePath().resolve(relativePath);
	}

	@Override
	public boolean isValid() {
		return event.isValid();
	}

	@Override
	public String toString() {
		String file = isDir() ? "DIR" : "FILE";
		return "Upload[" + getType() + " " + file + " " + getSource() + " to " + getDestination() + "]";
	}
}
