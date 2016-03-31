package de.qabel.desktop.daemon.sync.event;

import java.nio.file.Path;

import static de.qabel.desktop.daemon.sync.event.ChangeEvent.TYPE.*;

public abstract class AbstractChangeEvent extends AbstractWatchEvent implements ChangeEvent {
	protected final ChangeEvent.TYPE type;

	public AbstractChangeEvent(Path path, boolean isDirecotry, Long mtime, ChangeEvent.TYPE type) {
		super(path, isDirecotry, mtime);
		this.type = type;
	}

	@Override
    public ChangeEvent.TYPE getType() {
		return type;
	}

	@Override
	public abstract boolean isValid();

	@Override
	public boolean isUpdate() {
		return type == UPDATE;
	}

	@Override
	public boolean isCreate() {
		return type == CREATE;
	}

	@Override
	public boolean isDelete() {
		return type == DELETE;
	}

	@Override
	public boolean isShare() {
		return type == SHARE;
	}

	@Override
	public boolean isUnshare() {
		return type == UNSHARE;
	}
}
