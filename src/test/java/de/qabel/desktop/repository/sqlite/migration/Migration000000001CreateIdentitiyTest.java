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

    @Test(expected = SQLException.class)
    public void publicKeyIsUnique() throws Exception {
        migration.up();
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

        statement.setInt(1, 2);
        statement.setString(2, Hex.toHexString("12345678901234567890123456789012".getBytes()));
        statement.setString(3, Hex.toHexString("12345678901234567890123456789012".getBytes()));
        statement.setString(4, "my name2");
        statement.setString(5, "mail@example.com2");
        statement.setString(6, "012345678902");
        statement.execute();
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
            "INSERT INTO identity_drop_url (identity_id, url) VALUES (?, ?)"
        );
        statement.setInt(1, 1);
        statement.setString(2, "http://drop.example.com/someId");
        statement.execute();
        return statement;
    }

    @Test
    public void ignoresDuplicateUrls() throws Exception {
        migration.up();
        insertIdentity();

        insertDropUrl();
        insertDropUrl();

        assertEquals(1, countDropUrls());
    }

    @Test
    public void ignoresDuplicatePrefixes() throws Exception {
        migration.up();
        insertIdentity();

        insertPrefix();
        insertPrefix();

        assertEquals(1, countPrefixes());
    }

    public int countDropUrls() throws SQLException {
        Statement statement = connection.createStatement();
        statement.execute("SELECT count(*) FROM identity_drop_url");
        ResultSet resultSet = statement.getResultSet();
        resultSet.next();
        return resultSet.getInt(1);
    }

    public int countPrefixes() throws SQLException {
        Statement statement = connection.createStatement();
        statement.execute("SELECT count(*) FROM prefix");
        ResultSet resultSet = statement.getResultSet();
        resultSet.next();
        return resultSet.getInt(1);
    }

    @Test(expected = SQLException.class)
    public void dropUrlsRequireAnIdentity() throws Exception {
        migration.up();
        insertDropUrl();
    }

    @Test
    public void removedAfterDown() throws Exception {
        migration.up();
        insertIdentity();
        insertDropUrl();
        insertPrefix();
        migration.down();

        assertFalse("table identity was not removed", tableExists("identity"));
        assertFalse("table drop_url was not removed", tableExists("identity_drop_url"));
        assertFalse("table prefix was not removed", tableExists("prefix"));
    }

    private boolean tableExists(String tableName) throws SQLException {
        return new DefaultClientDatabase(connection).tableExists(tableName);
    }

    @Test
    public void hasPrefixes() throws Exception {
        migration.up();
        assertTrue(tableExists("prefix"));
        insertIdentity();
        insertDropUrl();

        PreparedStatement statement = insertPrefix();
        assertEquals(1, statement.getUpdateCount());
    }

    public PreparedStatement insertPrefix() throws SQLException {
        PreparedStatement statement = connection.prepareStatement(
            "INSERT INTO prefix (identity_id, prefix) VALUES (?, ?)"
        );
        statement.setInt(1, 1);
        statement.setString(2, "my/prefix");
        statement.execute();
        return statement;
    }

    @Test(expected = SQLException.class)
    public void prefixesRequireAnIdentity() throws Exception {
        migration.up();
        insertPrefix();
    }
}
