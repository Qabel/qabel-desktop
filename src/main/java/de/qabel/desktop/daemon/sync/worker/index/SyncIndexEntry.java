package de.qabel.desktop.daemon.sync.worker.index;

import java.io.Serializable;
import java.nio.file.Path;
import java.nio.file.Paths;

public class SyncIndexEntry implements Serializable {
	private String localPath;
	private Long localMtime;
	private boolean existing;

	public SyncIndexEntry(Path localPath, Long localMtime, boolean existing) {
		this.localPath = localPath.toString();
		this.localMtime = localMtime;
		this.existing = existing;
	}

	public Path getLocalPath() {
		return Paths.get(localPath);
	}

	public Long getLocalMtime() {
		return localMtime;
	}

	public boolean isExisting() {
		return existing;
	}

	public void setLocalMtime(Long localMtime) {
		this.localMtime = localMtime;
	}

	public void setExisting(boolean existing) {
		this.existing = existing;
	}
}
