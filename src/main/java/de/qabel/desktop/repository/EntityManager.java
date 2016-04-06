package de.qabel.desktop.repository;

import de.qabel.core.config.SyncSettingItem;

import java.util.WeakHashMap;

public class EntityManager {
    private WeakHashMap<Class, WeakHashMap<Integer, Object>> entities = new WeakHashMap<>();

    public boolean contains(Class clazz, int id) {
        if (!entities.containsKey(clazz)) {
            return false;
        }
        return entities.get(clazz).containsKey(id);
    }

    public synchronized <T> void put(Class<T> clazz, SyncSettingItem entity) {
        if (!entities.containsKey(clazz)) {
            entities.put(clazz, new WeakHashMap<>());
        }
        entities.get(clazz).put(entity.getId(), entity);
    }

    public <T> T get(Class<T> clazz, int id) {
        return (T)entities.get(clazz).get(id);
    }

    public void clear() {
        entities.clear();
    }
}
