package de.qabel.desktop.repository.inmemory;

import de.qabel.desktop.repository.ClientConfigRepository;
import de.qabel.desktop.repository.exception.EntityNotFoundExcepion;
import de.qabel.desktop.repository.exception.PersistenceException;

import java.util.HashMap;
import java.util.Map;

public class InMemoryClientConfigRepository implements ClientConfigRepository {
    private Map<String, String> values = new HashMap<>();

    @Override
    public String find(String key) throws EntityNotFoundExcepion, PersistenceException {
        if (!contains(key)) {
            throw new EntityNotFoundExcepion("no entry for " + key);
        }
        return values.get(key);
    }

    @Override
    public boolean contains(String key) throws PersistenceException {
        return values.containsKey(key);
    }

    @Override
    public void save(String key, String value) throws PersistenceException {
        values.put(key, value);
    }
}
