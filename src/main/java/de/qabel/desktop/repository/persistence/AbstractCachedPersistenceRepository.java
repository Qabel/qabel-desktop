package de.qabel.desktop.repository.persistence;

import de.qabel.core.config.Persistable;
import de.qabel.core.config.Persistence;

import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;

public class AbstractCachedPersistenceRepository<T extends Persistable> extends AbstractPersistenceRepository {
    protected Map<String, T> entityCache = new WeakHashMap<>();

    public AbstractCachedPersistenceRepository(Persistence<String> persistence) {
        super(persistence);
    }

    protected void syncWithCache(List<T> entities) {
        for (int i = 0; i < entities.size(); i++) {
            T identity = entities.get(i);
            if (isCached(identity)) {
                entities.set(i, fromCache(identity));
            } else {
                cache(identity);
            }
        }
    }

    protected T fromCache(T entity) {
        return fromCache(entity.getPersistenceID());
    }

    protected T fromCache(String key) {
        return entityCache.get(key);
    }

    protected boolean isCached(T entity) {
        return isCached(entity.getPersistenceID());
    }

    protected boolean isCached(String key) {
        return entityCache.containsKey(key);
    }

    protected void cache(T entity) {
        entityCache.put(entity.getPersistenceID(), entity);
    }
}
