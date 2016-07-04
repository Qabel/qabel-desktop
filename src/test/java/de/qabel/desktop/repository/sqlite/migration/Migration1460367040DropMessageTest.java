package de.qabel.desktop.repository.sqlite.migration;

import de.qabel.core.repository.sqlite.migration.AbstractMigration;
import org.junit.Test;

import java.sql.*;
import java.util.Date;

import static org.junit.Assert.*;

public class Migration1460367040DropMessageTest extends AbstractMigrationTest {
    @Override
    protected AbstractMigration createMigration(Connection connection) {
        return new Migration1460367040DropMessage(connection);
    }

    @Test
    public void persistsMessages() throws Exception {
        assertTrue(tableExists("drop_message"));
        int receiverId = insertIdentity();
        int senderId = insertContact();
        Date create = new Date();

        assertEquals(1, insertDropMessage(receiverId, senderId, create));
        try (PreparedStatement statement = connection.prepareStatement(
            "SELECT receiver_id, sender_id, sent, seen, created, payload_type, payload FROM drop_message"
        )) {
            try (ResultSet resultSet = statement.executeQuery()) {
                assertTrue(resultSet.next());
                int i = 1;
                assertEquals(receiverId, resultSet.getInt(i++));
                assertEquals(senderId, resultSet.getInt(i++));
                assertEquals(false, resultSet.getBoolean(i++));
                assertEquals(true, resultSet.getBoolean(i++));
                assertEquals(create.getTime(), resultSet.getTimestamp(i++).getTime());
                assertEquals("test_type", resultSet.getString(i++));
                assertEquals("message payload", resultSet.getString(i++));
                assertFalse(resultSet.next());
            }
        }
    }

    @Test
    public void acceptsAllEntities() throws Exception {
        int receiverId = insertContact();
        int senderId = insertIdentity();

        assertEquals(1, insertDropMessage(receiverId, senderId, new Date()));
    }

    @Test(expected = SQLException.class)
    public void requiresReceiver() throws Exception {
        int senderId = insertContact();
        int receiverId = senderId+1;
        insertDropMessage(receiverId, senderId, new Date());
    }

    @Test(expected = SQLException.class)
    public void requiresSender() throws Exception {
        int receiverId = insertIdentity();
        int senderId = receiverId+1;
        insertDropMessage(receiverId, senderId, new Date());
    }

    @Test
    public void cleansUpOnDown() throws Exception {
        insertDropMessage(insertIdentity(), insertContact(), new Date());
        migration.down();

        assertFalse(tableExists("drop_message"));
    }

    public int insertDropMessage(int receiverId, int senderId, Date create) throws SQLException {
        int updates;
        try (PreparedStatement statement = connection.prepareStatement(
            "INSERT INTO drop_message (receiver_id, sender_id, sent, seen, created, payload_type, payload)" +
                "VALUES (?, ?, ?, ?, ?, ?, ?)"
        )) {
            int i = 1;
            statement.setInt(i++, receiverId);
            statement.setInt(i++, senderId);
            statement.setBoolean(i++, false);
            statement.setBoolean(i++, true);
            statement.setTimestamp(i++, Timestamp.from(create.toInstant()));
            statement.setString(i++, "test_type");
            statement.setString(i++, "message payload");
            updates = statement.executeUpdate();
        }
        return updates;
    }

    private int insertContact() throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(
            "INSERT INTO contact (publicKey, alias) VALUES (?, ?)"
        )) {
            statement.setString(1, "contact pub");
            statement.setString(2, "my contact");
            statement.executeUpdate();
            try (ResultSet contactKeys = statement.getGeneratedKeys()) {
                contactKeys.next();
                return contactKeys.getInt(1);
            }
        }
    }

    private int insertIdentity() throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(
            "INSERT INTO contact (publicKey, alias) VALUES (?, ?)"
        )) {
            statement.setString(1, "identity pub");
            statement.setString(2, "my identity");
            statement.executeUpdate();
            try (ResultSet contactKeys = statement.getGeneratedKeys()) {
                contactKeys.next();
                int contactId = contactKeys.getInt(1);

                try (PreparedStatement insert = connection.prepareStatement(
                    "INSERT INTO identity (contact_id, privateKey) VALUES (?, ?)"
                )) {
                    statement.setInt(1, contactId);
                    statement.setString(2, "identity priv");
                    statement.executeUpdate();
                    try (ResultSet keys = insert.getGeneratedKeys()) {
                        keys.next();
                        return keys.getInt(1);
                    }
                }
            }
        }
    }
}
