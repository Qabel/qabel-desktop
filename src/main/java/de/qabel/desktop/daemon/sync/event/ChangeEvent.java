package de.qabel.desktop.daemon.sync.event;

public interface ChangeEvent extends WatchEvent {

    enum TYPE {CREATE, UPDATE, DELETE, SHARE, UNSHARE}

    TYPE getType();

    @Override
    boolean isValid();

    boolean isUpdate();

    boolean isCreate();

    boolean isDelete();

    boolean isShare();

    boolean isUnshare();
}
