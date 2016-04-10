package de.qabel.desktop.repository.sqlite.migration;

import org.junit.Test;
import org.spongycastle.util.encoders.Hex;

import java.sql.*;

import static org.junit.Assert.*;

public class Migration000000007ShareNotificationTest extends AbstractMigrationTest {

    @Override
    protected AbstractMigration createMigration(Connection connection) {
        return new Migration000000007ShareNotification(connection);
    }

    @Test
    public void createsTable() throws Exception {
        assertTrue(tableExists("share_notification"));

        Migration000000001CreateIdentitiyTest.insertIdentity(connection);
        assertEquals(1, insertNotification(1));
        assertEquals(1, countNotifications());
    }

    @Test(expected = SQLException.class)
    public void requiresValidIdentity() throws Exception {
        insertNotification(1);
    }

    @Test
    public void deletesWithIdentity() throws Exception {
        Migration000000001CreateIdentitiyTest.insertIdentity(connection);
        insertNotification(1);
        deleteIdentity(1);

        assertEquals(0, countNotifications());
    }

    private int countNotifications() throws SQLException {
        try (Statement statement = connection.createStatement()) {
            try (ResultSet resultSet = statement.executeQuery("SELECT COUNT(*) FROM share_notification")){
                resultSet.next();
                return resultSet.getInt(1);
            }
        }
    }

    private void deleteIdentity(int identityId) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement("DELETE FROM identity WHERE id = ?")) {
            statement.setInt(1, identityId);
            statement.execute();
        }
    }

    private int insertNotification(int identityId) throws SQLException {
        return insertShareNotification(identityId, connection);
    }

    public static int insertShareNotification(int identityId, Connection connection) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(
            "INSERT INTO share_notification (identity_id, url, key, message) VALUES (?, ?, ?, ?)"
        )) {
            int i = 1;
            statement.setInt(i++, identityId);
            statement.setString(i++, "http://url");
            statement.setString(i++, Hex.toHexString("some key".getBytes()));
            statement.setString(i++, "hey bud, take this share!");
            statement.execute();
            return statement.getUpdateCount();
        }
    }
}
