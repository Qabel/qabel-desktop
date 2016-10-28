package de.qabel.desktop.daemon.sync.worker.index.sqlite;

import de.qabel.core.config.Account;
import de.qabel.desktop.config.StubBoxSyncConfig;
import de.qabel.desktop.repository.sqlite.factory.SqliteConnectionFactorySpy;
import org.junit.Test;

import java.nio.file.Path;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

public class SqliteSyncIndexFactoryTest {
    @Test
    public void createsDifferentFilesForDifferentAccounts() throws Exception {
        SqliteSyncIndexFactory factory = new SqliteSyncIndexFactory();
        SqliteConnectionFactorySpy connectionFactory = new SqliteConnectionFactorySpy();
        factory.setConnectionFactory(connectionFactory);

        StubBoxSyncConfig config1 = new StubBoxSyncConfig();
        config1.account = new Account("provider1", "user1", "wayne");
        StubBoxSyncConfig config2 = new StubBoxSyncConfig();
        config2.account = new Account("provider1", "user2", "wayne");
        StubBoxSyncConfig config3 = new StubBoxSyncConfig();
        config3.account = new Account("provider2", "user1", "wayne");

        factory.getIndex(config1);
        Path file1 = connectionFactory.getDbFile();
        factory.getIndex(config2);
        Path file2 = connectionFactory.getDbFile();
        factory.getIndex(config3);
        Path file3 = connectionFactory.getDbFile();

        assertThat(file1.getFileName(), is(not(equalTo(file2.getFileName()))));
        assertThat(file1.getFileName(), is(not(equalTo(file3.getFileName()))));
        assertThat(file2.getFileName(), is(not(equalTo(file3.getFileName()))));
    }
}
