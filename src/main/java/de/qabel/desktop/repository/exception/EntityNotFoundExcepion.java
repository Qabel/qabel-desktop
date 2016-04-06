package de.qabel.desktop.repository.exception;

public class EntityNotFoundExcepion extends Exception {
    public EntityNotFoundExcepion(String message) {
        super(message);
    }

    public EntityNotFoundExcepion(String message, Throwable cause) {
        super(message, cause);
    }
}
