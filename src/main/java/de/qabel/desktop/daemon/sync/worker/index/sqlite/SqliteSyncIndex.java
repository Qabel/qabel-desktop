package de.qabel.desktop.daemon.sync.worker.index.sqlite;

import de.qabel.desktop.daemon.sync.worker.index.SyncIndex;
import de.qabel.desktop.daemon.sync.worker.index.SyncIndexEntry;
import de.qabel.desktop.daemon.sync.worker.index.SyncState;
import de.qabel.desktop.nio.boxfs.BoxFileSystem;
import de.qabel.desktop.nio.boxfs.BoxPath;
import de.qabel.desktop.util.LazyMap;
import de.qabel.desktop.util.LazyWeakHashMap;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;
import java.util.WeakHashMap;

public class SqliteSyncIndex implements SyncIndex {
    private LazyMap<BoxPath, SqliteSyncIndexEntry> entries = new LazyWeakHashMap<>();
    private Connection connection;

    public SqliteSyncIndex(Connection connection) {
        this.connection = connection;
        migrate(connection);
    }

    private void migrate(Connection connection) {
        try (PreparedStatement statement = connection.prepareStatement(
            "CREATE TABLE IF NOT EXISTS synced_state (" +
                "id INTEGER NOT NULL PRIMARY KEY," +
                "relative_path TEXT NOT NULL UNIQUE ON CONFLICT REPLACE," +
                "existing BOOLEAN NOT NULL DEFAULT false," +
                "mtime LONG DEFAULT NULL," +
                "size LONG DEFAULT NULL" +
            ")"
        )) {
            statement.execute();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to setup sync index: " + e.getMessage(), e);
        }
    }

    @Override
    public SyncIndexEntry get(BoxPath relativePath) {
        if (relativePath.isAbsolute()) {
            relativePath = BoxFileSystem.getRoot().relativize(relativePath);
        }
        return entries.getOrDefault(relativePath, r -> new SqliteSyncIndexEntry(r, loadEntry(r), connection));
    }

    private SyncState loadEntry(BoxPath relativePath) {
        try (PreparedStatement statement = connection.prepareStatement(
            "SELECT existing, mtime, size FROM synced_state WHERE relative_path = ?"
        )) {
            statement.setString(1, relativePath.toString());
            try (ResultSet resultSet = statement.executeQuery()) {
                if (!resultSet.next()) {
                    return new SyncState();
                }
                boolean existing = resultSet.getBoolean(1);
                Long mtime = resultSet.getLong(2);
                Long size = resultSet.getLong(3);
                return new SyncState(existing, mtime, size);
            }
        } catch (SQLException e) {
            throw new IllegalStateException("Failed to load entry for path " + relativePath, e);
        }
    }

    @Override
    public void clear() {
        entries.clear();
    }
}
