package de.qabel.desktop.repository;

import de.qabel.desktop.config.BoxSyncConfig;
import de.qabel.desktop.repository.exception.PersistenceException;

import java.util.List;

public interface BoxSyncConfigRepository {
    List<BoxSyncConfig> findAll() throws PersistenceException;
    void save(BoxSyncConfig config) throws PersistenceException;
    void delete(BoxSyncConfig config) throws PersistenceException;
}
