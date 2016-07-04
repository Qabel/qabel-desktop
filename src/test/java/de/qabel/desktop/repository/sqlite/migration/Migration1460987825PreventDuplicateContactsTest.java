package de.qabel.desktop.repository.sqlite.migration;

import de.qabel.core.repository.sqlite.migration.AbstractMigration;
import org.junit.Test;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class Migration1460987825PreventDuplicateContactsTest extends AbstractMigrationTest {
    @Override
    protected AbstractMigration createMigration(Connection connection) {
        return new Migration1460987825PreventDuplicateContacts(connection);
    }

    @Override
    public void setUp() throws Exception {
        super.setUp();

        execute("INSERT INTO contact (id, publicKey, alias) VALUES (1, 'abc', 'tester')");
        execute("INSERT INTO identity (id, privateKey, contact_id) VALUES (1, 'abc', 1)");
        execute("INSERT INTO contact (id, publicKey, alias) VALUES (2, 'cde', 'contact')");

        execute("INSERT INTO identity_contacts (identity_id, contact_id) VALUES (1, 2)");
    }

    @Test(expected = SQLException.class)
    public void preventsDuplicates() throws Exception {
        execute("INSERT INTO identity_contacts (identity_id, contact_id) VALUES (1, 2)");
    }

    private void execute(String query) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.execute();
        }
    }

    @Test
    public void cleansUp() throws Exception {
        migration.down();
        execute("INSERT INTO identity_contacts (identity_id, contact_id) VALUES (1, 2)");
    }

    @Test
    public void createsConsistentState() throws Exception {
        migration.down();
        execute("INSERT INTO identity_contacts (identity_id, contact_id) VALUES (1, 2)");
        migration.up();
    }
}
