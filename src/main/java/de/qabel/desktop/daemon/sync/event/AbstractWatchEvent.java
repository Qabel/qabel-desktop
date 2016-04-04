package de.qabel.desktop.daemon.sync.event;

import java.nio.file.Path;

public abstract class AbstractWatchEvent implements WatchEvent {
	protected final Long mtime;
	private final Path path;
	private final boolean isDir;

	public AbstractWatchEvent(Path path, boolean isDirectory, Long mtime) {
		this.path = path;
        isDir = isDirectory;
		this.mtime = mtime;
	}

	@Override
	public Path getPath() {
		return path;
	}

	@Override
	public boolean isDir() {
		return isDir;
	}

	@Override
	public Long getMtime() {
		return mtime;
	}
}
