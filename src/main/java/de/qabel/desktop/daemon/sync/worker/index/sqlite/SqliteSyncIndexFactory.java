package de.qabel.desktop.daemon.sync.worker.index.sqlite;

import de.qabel.desktop.config.BoxSyncConfig;
import de.qabel.desktop.daemon.sync.worker.index.SyncIndex;
import de.qabel.desktop.daemon.sync.worker.index.SyncIndexFactory;
import de.qabel.desktop.daemon.sync.worker.index.memory.InMemorySyncIndex;

import java.io.File;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class SqliteSyncIndexFactory implements SyncIndexFactory {
    private static final String DATABASE_FILENAME = ".qabel.index~";

    @Override
    public SyncIndex getIndex(BoxSyncConfig config) {
        Path dbFile = config.getLocalPath().resolve(DATABASE_FILENAME);
        try {
            Connection connection = DriverManager.getConnection("jdbc:sqlite://" + dbFile.toAbsolutePath());
            try (Statement statement = connection.createStatement()) {
                statement.execute("PRAGMA journal_mode=MEMORY");
            }
            return new SqliteSyncIndex(connection);
        } catch (SQLException e) {
            throw new IllegalArgumentException("unable to create index for config", e);
        }
    }
}
