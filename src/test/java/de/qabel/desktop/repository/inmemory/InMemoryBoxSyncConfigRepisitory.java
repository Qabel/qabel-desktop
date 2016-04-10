package de.qabel.desktop.repository.inmemory;

import de.qabel.desktop.config.BoxSyncConfig;
import de.qabel.desktop.repository.BoxSyncConfigRepository;
import de.qabel.desktop.repository.exception.PersistenceException;

import java.util.LinkedList;
import java.util.List;

public class InMemoryBoxSyncConfigRepisitory implements BoxSyncConfigRepository {
    private List<BoxSyncConfig> configs = new LinkedList<>();

    @Override
    public List<BoxSyncConfig> findAll() throws PersistenceException {
        return configs;
    }

    @Override
    public void save(BoxSyncConfig config) throws PersistenceException {
        if (configs.contains(config)) {
            return;
        }
        configs.add(config);
    }

    @Override
    public void delete(BoxSyncConfig config) throws PersistenceException {
        configs.remove(config);
    }
}
