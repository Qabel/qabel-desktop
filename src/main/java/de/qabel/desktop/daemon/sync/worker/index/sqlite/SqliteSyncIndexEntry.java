package de.qabel.desktop.daemon.sync.worker.index.sqlite;

import de.qabel.desktop.daemon.sync.worker.index.SyncIndexEntry;
import de.qabel.desktop.daemon.sync.worker.index.SyncState;
import de.qabel.desktop.nio.boxfs.BoxPath;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import static java.sql.Types.LONGNVARCHAR;

public class SqliteSyncIndexEntry extends SyncIndexEntry {
    private Connection connection;

    public SqliteSyncIndexEntry(BoxPath relativePath, SyncState syncedState, Connection connection) {
        super(relativePath, syncedState);
        this.connection = connection;
    }

    @Override
    public synchronized void setSyncedState(SyncState syncedState) {
        super.setSyncedState(syncedState);
        try (PreparedStatement statement = connection.prepareStatement(
            "INSERT INTO synced_state (relative_path, existing, mtime, size)" +
            "VALUES (?, ?, ?, ?)"
        )) {
            statement.setString(1, getRelativePath().toString());
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
            throw new RuntimeException("failed to persist new synced state of " + getRelativePath(), e);
        }
    }

    @Override
    protected void finalize() throws Throwable {
        super.finalize();
        connection.close();
    }
}
