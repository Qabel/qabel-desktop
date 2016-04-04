package de.qabel.desktop.exceptions;

public class QblStorageNotFound extends QblStorageException {

    public QblStorageNotFound(Throwable e) {
        super(e);
    }

    public QblStorageNotFound(String s) {
        super(s);
    }
}
