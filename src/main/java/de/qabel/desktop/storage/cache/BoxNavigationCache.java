package de.qabel.desktop.storage.cache;

import de.qabel.desktop.storage.BoxFolder;
import de.qabel.desktop.storage.BoxNavigation;

import java.util.Map;
import java.util.WeakHashMap;

public class BoxNavigationCache<C extends BoxNavigation> {
	private Map<String, C> navs = new WeakHashMap<>();

	public void cache(BoxFolder folder, C nav) {
		navs.put(folder.ref, nav);
	}

	public boolean has(BoxFolder folder) {
		return navs.containsKey(folder.ref);
	}

	public C get(BoxFolder folder) {
		return navs.get(folder.ref);
	}

	public void remove(BoxFolder folder) {
		navs.remove(folder.ref);
	}

	public Iterable<C> getAll() {
		return navs.values();
	}
}
