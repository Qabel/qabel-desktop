package de.qabel.desktop.repository.sqlite.migration;

import de.qabel.desktop.repository.sqlite.DefaultClientDatabase;
import org.junit.Test;
import org.spongycastle.util.encoders.Hex;

import java.sql.*;

import static org.junit.Assert.*;

public class Migration000000001CreateIdentitiyTest extends AbstractSqliteTest {
    private Migration000000001CreateIdentitiy migration;

    @Override
    public void setUp() throws Exception {
        super.setUp();
        migration = new Migration000000001CreateIdentitiy(connection);
    }

    @Test
    public void existsAfterUp() throws Exception {
        migration.up();
        PreparedStatement statement = insertIdentity();
        assertEquals(1, statement.getUpdateCount());
        assertTrue(connection.prepareStatement("SELECT * FROM identity").execute());
    }

    public PreparedStatement insertIdentity() throws SQLException {
        PreparedStatement statement = connection.prepareStatement(
            "INSERT INTO identity (id, publicKey, privateKey, alias, email, phone) VALUES (?, ?, ?, ?, ?, ?)"
        );
        statement.setInt(1, 1);
        statement.setString(2, Hex.toHexString("12345678901234567890123456789012".getBytes()));
        statement.setString(3, Hex.toHexString("12345678901234567890123456789012".getBytes()));
        statement.setString(4, "my name");
        statement.setString(5, "mail@example.com");
        statement.setString(6, "01234567890");
        statement.execute();
        return statement;
    }

    @Test
    public void hasDropUrls() throws Exception {
        migration.up();
        insertIdentity();
        PreparedStatement statement = insertDropUrl();
        assertEquals(1, statement.getUpdateCount());
    }

    public PreparedStatement insertDropUrl() throws SQLException {
        PreparedStatement statement = connection.prepareStatement(
            "INSERT INTO drop_url (id, identity_id, url) VALUES (?, ?, ?)"
        );
        statement.setInt(1, 1);
        statement.setInt(2, 1);
        statement.setString(3, "http://drop.example.com/someId");
        statement.execute();
        return statement;
    }

    @Test(expected = SQLException.class)
    public void dropUrlsAreOnlyAllowedOnExistingIdentites() throws Exception {
        migration.up();
        insertDropUrl();
    }

    @Test
    public void removedAfterDown() throws Exception {
        migration.up();
        insertIdentity();
        insertDropUrl();
        migration.down();

        assertFalse("table identity was not removed", tableExists("identity"));
        assertFalse("table drop_url was not removed", tableExists("drop_url"));
    }

    private boolean tableExists(String tableName) throws SQLException {
        return new DefaultClientDatabase(connection).tableExists(tableName);
    }
}
