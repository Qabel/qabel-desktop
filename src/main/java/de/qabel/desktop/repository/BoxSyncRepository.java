package de.qabel.desktop.repository;

import de.qabel.desktop.config.BoxSyncConfig;
import de.qabel.desktop.repository.exception.PersistenceException;

import java.util.List;
import java.util.function.Consumer;

public interface BoxSyncRepository {
    List<BoxSyncConfig> findAll() throws PersistenceException;
    void save(BoxSyncConfig config) throws PersistenceException;
    void delete(BoxSyncConfig config) throws PersistenceException;

    void onAdd(Consumer<BoxSyncConfig> consumer);
    void onDelete(Consumer<BoxSyncConfig> consumer);
}
