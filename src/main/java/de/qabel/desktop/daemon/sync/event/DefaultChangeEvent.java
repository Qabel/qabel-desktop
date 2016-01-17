package de.qabel.desktop.daemon.sync.event;

import java.nio.file.Path;

public class DefaultChangeEvent extends AbstractWatchEvent implements ChangeEvent {
	private final TYPE type;
	private final long mtime;

	public DefaultChangeEvent(Path path, TYPE type) {
		super(path);
		this.type = type;
		this.mtime = path.toFile().lastModified();
	}

	@Override
	public TYPE getType() {
		return type;
	}

	@Override
	public boolean isValid() {
		return getPath().toFile().lastModified() == mtime;
	}

	@Override
	public boolean isUpdate() {
		return type == TYPE.UPDATE;
	}

	@Override
	public boolean isCreate() {
		return type == TYPE.CREATE;
	}

	@Override
	public boolean isDelete() {
		return type == TYPE.DELETE;
	}
}
