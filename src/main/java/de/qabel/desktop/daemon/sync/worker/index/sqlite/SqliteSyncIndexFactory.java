package de.qabel.desktop.daemon.sync.worker.index.sqlite;

import de.qabel.desktop.config.BoxSyncConfig;
import de.qabel.desktop.daemon.sync.worker.index.SyncIndex;
import de.qabel.desktop.daemon.sync.worker.index.SyncIndexEntry;
import de.qabel.desktop.daemon.sync.worker.index.SyncIndexEntryRepository;
import de.qabel.desktop.daemon.sync.worker.index.SyncIndexFactory;
import de.qabel.desktop.daemon.sync.worker.index.memory.InMemorySyncIndex;
import de.qabel.desktop.nio.boxfs.BoxPath;
import de.qabel.desktop.repository.EntityManager;
import de.qabel.desktop.repository.GenericEntityManager;
import de.qabel.desktop.repository.LambdaEntityManager;
import de.qabel.desktop.repository.sqlite.MigrationException;

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
            DesktopSyncDatabase database = new DesktopSyncDatabase(connection);
            database.migrate();
            SyncIndexEntryRepository repo = new SqliteSyncIndexEntryRepository(database, new SyncIndexEntryHydrator(
                new LambdaEntityManager<>(SyncIndexEntry::getRelativePath)
            ));
            return new SqliteSyncIndex(repo);
        } catch (SQLException | MigrationException e) {
            throw new IllegalArgumentException("unable to create index for config", e);
        }
    }
}
