package de.qabel.desktop.daemon.sync.worker.index.sqlite.migration;

import de.qabel.core.repository.sqlite.migration.AbstractMigration;

import java.sql.Connection;
import java.sql.SQLException;

public class Migration1462872507CreateSyncedState extends AbstractMigration {
    public Migration1462872507CreateSyncedState(Connection connection) {
        super(connection);
    }

    @Override
    public long getVersion() {
        return 1462872507L;
    }

    @Override
    public void up() throws SQLException {
        execute(
            "CREATE TABLE IF NOT EXISTS synced_state (" +
                "id INTEGER NOT NULL PRIMARY KEY," +
                "relative_path TEXT NOT NULL UNIQUE ON CONFLICT REPLACE," +
                "existing BOOLEAN NOT NULL DEFAULT false," +
                "mtime LONG DEFAULT NULL," +
                "size LONG DEFAULT NULL" +
            ")"
        );
    }

    @Override
    public void down() throws SQLException {
        execute(
            "DROP TABLE IF EXISTS synced_state"
        );
    }
}
