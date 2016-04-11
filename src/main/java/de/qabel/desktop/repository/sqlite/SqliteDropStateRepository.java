package de.qabel.desktop.repository.sqlite;

import de.qabel.desktop.repository.DropStateRepository;
import de.qabel.desktop.repository.exception.EntityNotFoundExcepion;
import de.qabel.desktop.repository.exception.PersistenceException;

public class SqliteDropStateRepository implements DropStateRepository {
    @Override
    public String getDropState(String drop) throws EntityNotFoundExcepion, PersistenceException {
        return null;
    }

    @Override
    public void setDropState(String drop, String state) throws PersistenceException {

    }
}
