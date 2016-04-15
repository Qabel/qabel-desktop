package de.qabel.desktop.repository.inmemory;

import de.qabel.desktop.repository.DropStateRepository;
import de.qabel.desktop.repository.exception.EntityNotFoundExcepion;
import de.qabel.desktop.repository.exception.PersistenceException;

import java.util.HashMap;
import java.util.Map;

public class InMemoryDropStateRepository implements DropStateRepository {
    private Map<String, String> states = new HashMap<>();

    @Override
    public String getDropState(String drop) throws EntityNotFoundExcepion, PersistenceException {
        if (!states.containsKey(drop)) {
            throw new EntityNotFoundExcepion("drop not found: " + drop);
        }
        return states.get(drop);
    }

    @Override
    public void setDropState(String drop, String state) throws PersistenceException {
        states.put(drop, state);
    }
}
