package de.qabel.desktop.daemon.sync.worker.index.sqlite;

import de.qabel.core.repository.exception.EntityNotFoundException;
import de.qabel.core.repository.exception.PersistenceException;
import de.qabel.core.util.LazyMap;
import de.qabel.core.util.LazyWeakHashMap;
import de.qabel.desktop.daemon.sync.worker.index.SyncIndex;
import de.qabel.desktop.daemon.sync.worker.index.SyncIndexEntry;
import de.qabel.desktop.daemon.sync.worker.index.SyncIndexEntryRepository;
import de.qabel.desktop.daemon.sync.worker.index.SyncState;
import de.qabel.desktop.nio.boxfs.BoxFileSystem;
import de.qabel.desktop.nio.boxfs.BoxPath;

public class SqliteSyncIndex implements SyncIndex {
    private LazyMap<BoxPath, SyncIndexEntry> entries = new LazyWeakHashMap<>();
    private SyncIndexEntryRepository repo;

    public SqliteSyncIndex(SyncIndexEntryRepository repo) {
        this.repo = repo;
    }

    @Override
    public SyncIndexEntry get(BoxPath relativePath) {
        if (relativePath.isAbsolute()) {
            relativePath = BoxFileSystem.getRoot().relativize(relativePath);
        }
        return entries.getOrDefault(relativePath, this::loadEntry);
    }

    private synchronized SyncIndexEntry loadEntry(BoxPath relativePath) {
        try {
            try {
                return repo.find(relativePath);
            } catch (EntityNotFoundException e) {
                SyncIndexEntry entry = new SqliteSyncIndexEntry(relativePath, new SyncState(), repo);
                repo.save(entry);
                return entry;
            }
        } catch (PersistenceException e) {
            throw new IllegalStateException("Failed to load entry for path " + relativePath, e);
        }
    }

    @Override
    public void clear() {
        entries.clear();
    }
}
