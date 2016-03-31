package de.qabel.desktop.exceptions;

import de.qabel.core.exceptions.QblException;

public class QblStorageException extends QblException {
    public QblStorageException(Throwable e) {
        super(e.getMessage());
    }

    public QblStorageException(String s) {
        super(s);
    }

    public QblStorageException(String s, Exception e) {
        super(s, e);
    }
}
