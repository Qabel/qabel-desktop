package de.qabel.desktop.daemon.sync.worker.index.sqlite;

import de.qabel.core.config.Account;
import de.qabel.core.repository.sqlite.MigrationException;
import de.qabel.desktop.config.BoxSyncConfig;
import de.qabel.desktop.daemon.sync.worker.index.SyncIndex;
import de.qabel.desktop.daemon.sync.worker.index.SyncIndexEntry;
import de.qabel.desktop.daemon.sync.worker.index.SyncIndexEntryRepository;
import de.qabel.desktop.daemon.sync.worker.index.SyncIndexFactory;
import de.qabel.desktop.repository.LambdaEntityManager;
import de.qabel.desktop.repository.sqlite.factory.SqliteConnectionFactory;
import org.apache.commons.codec.digest.DigestUtils;

import java.nio.file.Path;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

public class SqliteSyncIndexFactory implements SyncIndexFactory {
    private static final String DATABASE_FILENAME = ".qabel.%s.index~";
    private SqliteConnectionFactory connectionFactory = new SqliteConnectionFactory();

    @Override
    public SyncIndex getIndex(BoxSyncConfig config) {
        Account account = config.getAccount();
        String accountHash = DigestUtils.sha256Hex(account.getUser() + "@" + account.getProvider()).substring(0, 10);
        Path dbFile = config.getLocalPath().resolve(String.format(DATABASE_FILENAME, accountHash));
        try {
            Connection connection = connectionFactory.getConnection(dbFile);
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

    public void setConnectionFactory(SqliteConnectionFactory connectionFactory) {
        this.connectionFactory = connectionFactory;
    }
}
