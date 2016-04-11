package de.qabel.desktop.repository.sqlite;

import de.qabel.desktop.config.BoxSyncConfig;
import de.qabel.desktop.repository.BoxSyncConfigRepository;
import de.qabel.desktop.repository.exception.PersistenceException;

import java.util.List;
import java.util.function.Consumer;

public class SqliteBoxSyncConfigRepository implements BoxSyncConfigRepository {
    @Override
    public List<BoxSyncConfig> findAll() throws PersistenceException {
        return null;
    }

    @Override
    public void save(BoxSyncConfig config) throws PersistenceException {

    }

    @Override
    public void delete(BoxSyncConfig config) throws PersistenceException {

    }

    @Override
    public void onAdd(Consumer<BoxSyncConfig> consumer) {

    }

    @Override
    public void onDelete(Consumer<BoxSyncConfig> consumer) {

    }
}
