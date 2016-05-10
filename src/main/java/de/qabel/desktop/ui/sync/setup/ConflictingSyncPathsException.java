package de.qabel.desktop.ui.sync.setup;

public class ConflictingSyncPathsException extends IllegalArgumentException {
    public ConflictingSyncPathsException() {
    }

    public ConflictingSyncPathsException(String s) {
        super(s);
    }

    public ConflictingSyncPathsException(String message, Throwable cause) {
        super(message, cause);
    }

    public ConflictingSyncPathsException(Throwable cause) {
        super(cause);
    }
}
