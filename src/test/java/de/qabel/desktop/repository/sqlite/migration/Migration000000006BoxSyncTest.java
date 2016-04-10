package de.qabel.desktop.repository.sqlite.migration;

import org.junit.Test;

import java.sql.*;

import static org.junit.Assert.*;

public class Migration000000006BoxSyncTest extends AbstractMigrationTest {

    @Override
    protected AbstractMigration createMigration(Connection connection) {
        return new Migration000000006BoxSync(connection);
    }

    @Test
    public void createsBoxSyncTable() throws Exception {
        assertTrue(tableExists("box_sync"));

        Migration000000001CreateIdentitiyTest.insertIdentity(connection);
        Migration000000003CreateAccountTest.insertAccount("p", "u", "a", connection);
        assertEquals(1, insertSync());

        try (PreparedStatement statement = connection.prepareStatement("SELECT paused FROM box_sync WHERE id = 1")) {
            try (ResultSet resultSet = statement.executeQuery()) {
                resultSet.next();
                assertEquals(false, resultSet.getBoolean(1));
            }
        }
    }

    @Test(expected = SQLException.class)
    public void requiresValidIdentity() throws Exception {
        Migration000000003CreateAccountTest.insertAccount("p", "u", "a", connection);
        insertSync();
    }

    @Test(expected = SQLException.class)
    public void requiresValidAccount() throws Exception {
        Migration000000001CreateIdentitiyTest.insertIdentity(connection);
        insertSync();
    }

    @Test(expected = SQLException.class)
    public void preventsDuplicateLocalPaths() throws Exception {
        Migration000000001CreateIdentitiyTest.insertIdentity(connection);
        Migration000000003CreateAccountTest.insertAccount("p", "u", "a", connection);
        insertSync();
        insertSync();
    }

    @Test
    public void cleansUpOnDown() throws Exception {
        Migration000000001CreateIdentitiyTest.insertIdentity(connection);
        Migration000000003CreateAccountTest.insertAccount("p", "u", "a", connection);
        insertSync();

        migration.down();

        assertFalse(tableExists("box_sync"));
    }

    public int insertSync() throws SQLException {
        return insertSync("sync name", 1, 1, "local path", "remote path", connection);
    }

    public static int insertSync(String name, int accountId, int identityId, String localPath, String remotePath, Connection connection) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(
            "INSERT INTO box_sync (name, account_id, identity_id, local_path, remote_path) VALUES (?, ?, ?, ?, ?)"
        )) {
            int i = 1;
            statement.setString(i++, name);
            statement.setInt(i++, accountId);
            statement.setInt(i++, identityId);
            statement.setString(i++, localPath);
            statement.setString(i++, remotePath);
            statement.execute();
            return statement.getUpdateCount();
        }
    }
}
