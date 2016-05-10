package de.qabel.desktop.daemon.sync.worker.index.memory;


import de.qabel.desktop.daemon.sync.worker.index.SyncIndex;
import de.qabel.desktop.daemon.sync.worker.index.SyncIndexEntry;
import de.qabel.desktop.nio.boxfs.BoxFileSystem;
import de.qabel.desktop.nio.boxfs.BoxPath;

import java.util.HashMap;
import java.util.Map;

public class InMemorySyncIndex implements SyncIndex {
    private final Map<String, SyncIndexEntry> index = new HashMap<>();

    @Override
    public SyncIndexEntry get(BoxPath relativePath) {
        if (relativePath.isAbsolute()) {
            relativePath = BoxFileSystem.getRoot().relativize(relativePath);
        }
        String key = relativePath.toString();
        synchronized (index) {
            if (!index.containsKey(key)) {
                index.put(key, new SyncIndexEntry(relativePath));
            }
            return index.get(key);
        }
    }

    @Override
    public void clear() {
        index.clear();
    }
}
