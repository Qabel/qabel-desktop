package de.qabel.desktop.storage.cache;

import de.qabel.box.storage.BoxFolder;
import de.qabel.box.storage.BoxNavigation;

import java.util.Map;
import java.util.WeakHashMap;

public class BoxNavigationCache<C extends BoxNavigation> {
    private Map<String, C> navs = new WeakHashMap<>();

    public void cache(BoxFolder folder, C nav) {
        navs.put(folder.getRef(), nav);
    }

    public boolean has(BoxFolder folder) {
        return navs.containsKey(folder.getRef());
    }

    public C get(BoxFolder folder) {
        return navs.get(folder.getRef());
    }

    public void remove(BoxFolder folder) {
        navs.remove(folder.getRef());
    }

    public Iterable<C> getAll() {
        return navs.values();
    }
}
