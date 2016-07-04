package de.qabel.desktop.daemon.sync.worker.index.sqlite;

import de.qabel.core.repository.exception.PersistenceException;
import de.qabel.desktop.daemon.sync.worker.index.SyncIndexEntry;
import de.qabel.desktop.daemon.sync.worker.index.SyncIndexEntryRepository;
import de.qabel.desktop.daemon.sync.worker.index.SyncState;
import de.qabel.desktop.nio.boxfs.BoxPath;

public class SqliteSyncIndexEntry extends SyncIndexEntry {
    private SyncIndexEntryRepository repo;

    public SqliteSyncIndexEntry(BoxPath relativePath, SyncState syncedState, SyncIndexEntryRepository repo) {
        super(relativePath, syncedState);
        this.repo = repo;
    }

    @Override
    public synchronized void setSyncedState(SyncState syncedState) {
        super.setSyncedState(syncedState);
        try {
            repo.save(this);
        } catch (PersistenceException e) {
            throw new RuntimeException("failed to persist new synced state of " + getRelativePath(), e);
        }
    }
}
