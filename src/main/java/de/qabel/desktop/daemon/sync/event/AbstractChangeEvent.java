package de.qabel.desktop.daemon.sync.event;

import java.nio.file.Path;

public class AbstractChangeEvent extends AbstractWatchEvent {
	protected final ChangeEvent.TYPE type;
	protected final long mtime;

	public AbstractChangeEvent(Path path, ChangeEvent.TYPE type) {
		super(path);
		this.type = type;
		this.mtime = path.toFile().lastModified();
	}

	public ChangeEvent.TYPE getType() {
		return type;
	}

	@Override
	public boolean isValid() {
		return getPath().toFile().lastModified() == mtime;
	}

	public boolean isUpdate() {
		return type == ChangeEvent.TYPE.UPDATE;
	}

	public boolean isCreate() {
		return type == ChangeEvent.TYPE.CREATE;
	}

	public boolean isDelete() {
		return type == ChangeEvent.TYPE.DELETE;
	}
}
