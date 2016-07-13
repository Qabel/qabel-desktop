package de.qabel.desktop.daemon.sync.worker.index.memory;


import de.qabel.core.util.LazyHashMap;
import de.qabel.desktop.daemon.sync.worker.index.SyncIndex;
import de.qabel.desktop.daemon.sync.worker.index.SyncIndexEntry;
import de.qabel.desktop.nio.boxfs.BoxFileSystem;
import de.qabel.desktop.nio.boxfs.BoxPath;

public class InMemorySyncIndex implements SyncIndex {
    private final LazyHashMap<BoxPath, SyncIndexEntry> index = new LazyHashMap<>();

    @Override
    public SyncIndexEntry get(BoxPath relativePath) {
        if (relativePath.isAbsolute()) {
            relativePath = BoxFileSystem.getRoot().relativize(relativePath);
        }
        return index.getOrDefault(relativePath, SyncIndexEntry::new);
    }

    @Override
    public void clear() {
        index.clear();
    }
}
