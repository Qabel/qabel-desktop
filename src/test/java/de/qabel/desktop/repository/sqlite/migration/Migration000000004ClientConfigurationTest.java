package de.qabel.desktop.repository.sqlite.migration;

import org.junit.Test;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import static org.junit.Assert.*;

public class Migration000000004ClientConfigurationTest extends AbstractMigrationTest {

    @Override
    protected AbstractMigration createMigration(Connection connection) {
        return new Migration000000004ClientConfiguration(connection);
    }

    @Test
    public void createsKeyValueTable() throws Exception {
        assertTrue(tableExists("client_configuration"));
        assertEquals(1, insertPair("user", "tester"));
    }

    public int insertPair(String key, String value) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(
            "INSERT INTO client_configuration (key, value) VALUES (?, ?)"
        )) {
            statement.setString(1, key);
            statement.setString(2, value);
            statement.execute();
            return statement.getUpdateCount();
        }
    }

    @Test
    public void overwritesValues() throws Exception {
        insertPair("key", "value1");
        assertEquals("insert was ignored", 1, insertPair("key", "value2"));

        try (PreparedStatement statement = connection.prepareStatement(
            "SELECT value FROM client_configuration WHERE key = ?"
        )) {
            statement.setString(1, "key");
            try (ResultSet resultSet = statement.executeQuery()) {
                resultSet.next();
                assertEquals("value2", resultSet.getString(1));
                assertFalse("key is not unique", resultSet.next());
            }
        }
    }

    @Test
    public void cleansUpTables() throws Exception {
        insertPair("some", "content");
        migration.down();

        assertFalse(tableExists("client_configuration"));
    }
}
