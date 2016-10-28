package de.qabel.desktop.daemon.sync.worker.index;

import de.qabel.core.repository.exception.EntityNotFoundException;
import de.qabel.core.repository.exception.PersistenceException;
import de.qabel.desktop.nio.boxfs.BoxPath;

public interface SyncIndexEntryRepository {
    SyncIndexEntry find(BoxPath relativeRemotePath) throws EntityNotFoundException, PersistenceException;
    void save(SyncIndexEntry entry) throws PersistenceException;
}
