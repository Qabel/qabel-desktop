package de.qabel.desktop.repository.inmemory;

import de.qabel.desktop.config.BoxSyncConfig;
import de.qabel.desktop.repository.BoxSyncRepository;
import de.qabel.desktop.repository.exception.PersistenceException;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;

public class InMemoryBoxSyncRepisitory implements BoxSyncRepository {
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
        addListener.forEach(c -> c.accept(config));
    }

    @Override
    public void delete(BoxSyncConfig config) throws PersistenceException {
        configs.remove(config);
        deleteListener.forEach(c -> c.accept(config));
    }

    private List<Consumer<BoxSyncConfig>> addListener = new CopyOnWriteArrayList<>();
    private List<Consumer<BoxSyncConfig>> deleteListener = new CopyOnWriteArrayList<>();
    @Override
    public void onAdd(Consumer<BoxSyncConfig> consumer) {
        addListener.add(consumer);
    }

    @Override
    public void onDelete(Consumer<BoxSyncConfig> consumer) {
        deleteListener.add(consumer);
    }
}
