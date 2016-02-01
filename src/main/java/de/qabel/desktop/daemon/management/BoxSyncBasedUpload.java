package de.qabel.desktop.daemon.management;

import de.qabel.desktop.config.BoxSyncConfig;
import de.qabel.desktop.daemon.sync.event.ChangeEvent;
import de.qabel.desktop.daemon.sync.event.WatchEvent;
import de.qabel.desktop.storage.BoxVolume;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.TimeUnit;

public class BoxSyncBasedUpload extends AbstractBoxSyncBasedTransaction implements Upload {
	public static Logger logger = LoggerFactory.getLogger(BoxSyncBasedUpload.class.getSimpleName());

	private long stagingDelayMills = TimeUnit.SECONDS.toMillis(2);

	public BoxSyncBasedUpload(BoxVolume volume, BoxSyncConfig boxSyncConfig,  WatchEvent event) {
		super(volume, event, boxSyncConfig);

		if (hasSize(event)) {
			try {
				setSize(Files.size(event.getPath()));
			} catch (IOException e) {
				logger.warn("failed to get filesize: " + e.getMessage(), e);
			}
		}
	}

	protected static boolean hasSize(WatchEvent event) {
		return !Files.isDirectory(event.getPath())
				&& (
				!(event instanceof ChangeEvent)
						|| ((ChangeEvent) event).getType() != ChangeEvent.TYPE.DELETE
		);
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
	public long getStagingDelayMillis() {
		return stagingDelayMills;
	}

	public void setStagingDelayMills(long stagingDelayMills) {
		this.stagingDelayMills = stagingDelayMills;
	}

	@Override
	public String toString() {
		String file = isDir() ? "DIR" : "FILE";
		return "Upload[" + getType() + " " + file + " " + getSource() + " to " + getDestination() + "]";
	}
}
