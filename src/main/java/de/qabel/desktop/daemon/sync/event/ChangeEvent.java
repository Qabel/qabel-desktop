package de.qabel.desktop.daemon.sync.event;

public interface ChangeEvent extends WatchEvent {
	enum TYPE {CREATE, UPDATE, DELETE}

	TYPE getType();

	boolean isValid();

	boolean isUpdate();

	boolean isCreate();

	boolean isDelete();
}
