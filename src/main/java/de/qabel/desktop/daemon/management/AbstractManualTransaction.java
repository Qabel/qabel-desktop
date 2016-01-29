package de.qabel.desktop.daemon.management;

import de.qabel.desktop.storage.BoxVolume;

import java.nio.file.Path;

public abstract class AbstractManualTransaction extends AbstractTransaction {
	protected final TYPE type;
	protected final BoxVolume volume;
	protected final Path source;
	protected final Path destination;
	protected final boolean isDir;

	public AbstractManualTransaction(Long mtime, boolean isDir, Path destination, Path source, TYPE type, BoxVolume volume) {
		super(mtime);
		this.isDir = isDir;
		this.destination = destination;
		this.source = source;
		this.type = type;
		this.volume = volume;
	}

	@Override
	public TYPE getType() {
		return type;
	}

	@Override
	public BoxVolume getBoxVolume() {
		return volume;
	}

	@Override
	public Path getSource() {
		return source;
	}

	@Override
	public Path getDestination() {
		return destination;
	}

	@Override
	public boolean isValid() {
		return true;
	}

	@Override
	public boolean isDir() {
		return isDir;
	}

	@Override
	public long getStagingDelayMillis() {
		return 0;
	}
}
