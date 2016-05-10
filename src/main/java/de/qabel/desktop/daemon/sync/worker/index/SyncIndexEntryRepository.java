package de.qabel.desktop.daemon.sync.worker.index;

import de.qabel.desktop.nio.boxfs.BoxPath;
import de.qabel.desktop.repository.exception.EntityNotFoundException;
import de.qabel.desktop.repository.exception.PersistenceException;

public interface SyncIndexEntryRepository {
    SyncIndexEntry find(BoxPath relativeRemotePath) throws EntityNotFoundException, PersistenceException;
    void save(SyncIndexEntry entry) throws PersistenceException;
}
