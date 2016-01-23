package de.qabel.desktop.daemon.sync.event;

import org.slf4j.LoggerFactory;

import java.nio.file.Path;

public abstract class AbstractChangeEvent extends AbstractWatchEvent {
	protected final ChangeEvent.TYPE type;

	public AbstractChangeEvent(Path path, boolean isDirecotry, Long mtime, ChangeEvent.TYPE type) {
		super(path, isDirecotry, mtime);
		this.type = type;
	}

	public ChangeEvent.TYPE getType() {
		return type;
	}

	@Override
	public abstract boolean isValid();

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
