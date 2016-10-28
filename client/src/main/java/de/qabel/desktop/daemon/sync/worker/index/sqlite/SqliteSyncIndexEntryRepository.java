package de.qabel.desktop.daemon.sync.worker.index.sqlite;

import de.qabel.core.repository.GenericEntityManager;
import de.qabel.core.repository.exception.EntityNotFoundException;
import de.qabel.core.repository.exception.PersistenceException;
import de.qabel.core.repository.sqlite.AbstractSqliteRepository;
import de.qabel.core.repository.sqlite.ClientDatabase;
import de.qabel.core.repository.sqlite.Hydrator;
import de.qabel.desktop.daemon.sync.worker.index.SyncIndexEntry;
import de.qabel.desktop.daemon.sync.worker.index.SyncIndexEntryRepository;
import de.qabel.desktop.daemon.sync.worker.index.SyncState;
import de.qabel.desktop.nio.boxfs.BoxPath;

import java.sql.PreparedStatement;
import java.sql.SQLException;

import static java.sql.Types.LONGNVARCHAR;

public class SqliteSyncIndexEntryRepository extends AbstractSqliteRepository<SyncIndexEntry>
    implements SyncIndexEntryRepository{
    public SqliteSyncIndexEntryRepository(ClientDatabase database, Hydrator<SyncIndexEntry> hydrator) {
        super(database, hydrator, "synced_state");
    }

    public SqliteSyncIndexEntryRepository(ClientDatabase database, GenericEntityManager<BoxPath, SyncIndexEntry> em) {
        this(database, new SyncIndexEntryHydrator(em));
    }

    @Override
    public SyncIndexEntry find(BoxPath relativeRemotePath) throws EntityNotFoundException, PersistenceException {
        return findBy("relative_path=?", relativeRemotePath.toString());
    }

    @Override
    public void save(SyncIndexEntry entry) throws PersistenceException {
        try (PreparedStatement statement = database.prepare(
            "INSERT INTO synced_state (relative_path, existing, mtime, size)" +
                "VALUES (?, ?, ?, ?)"
        )) {
            SyncState syncedState = entry.getSyncedState();
            statement.setString(1, entry.getRelativePath().toString());
            statement.setBoolean(2, syncedState.isExisting());
            if (syncedState.getMtime() == null) {
                statement.setNull(3, LONGNVARCHAR);
            } else {
                statement.setLong(3, syncedState.getMtime());
            }
            if (syncedState.getSize() == null) {
                statement.setNull(4, LONGNVARCHAR);
            } else {
                statement.setLong(4, syncedState.getSize());
            }
            statement.execute();
        } catch (SQLException e) {
            throw new RuntimeException("failed to persist new synced state of " + entry.getRelativePath(), e);
        }
    }
}
