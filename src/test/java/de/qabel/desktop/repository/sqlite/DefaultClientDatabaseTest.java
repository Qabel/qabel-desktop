package de.qabel.desktop.repository.sqlite;

import de.qabel.desktop.repository.sqlite.migration.AbstractSqliteTest;
import de.qabel.desktop.repository.sqlite.migration.FailingMigration;
import de.qabel.desktop.repository.sqlite.migration.Migration1460367000CreateIdentitiy;
import org.junit.Test;

import java.sql.SQLException;

import static org.junit.Assert.*;

public class DefaultClientDatabaseTest extends AbstractSqliteTest {
    private DefaultClientDatabase database;

    @Override
    public void setUp() throws Exception {
        super.setUp();
        database = new DefaultClientDatabase(connection);
    }

    @Test
    public void startsWithVersion0() throws SQLException {
        assertEquals(0, database.getVersion());
        assertFalse("migration was executed unintentionally", database.tableExists("identity"));
    }

    @Test
    public void migratesVersion() throws Exception {
        database.migrateTo(Migration1460367000CreateIdentitiy.VERSION);
        assertEquals(Migration1460367000CreateIdentitiy.VERSION, database.getVersion());
        assertTrue("migration was not executed", database.tableExists("identity"));
    }

    @Test
    public void ignoresExecutedMigrations() throws Exception {
        database.migrateTo(Migration1460367000CreateIdentitiy.VERSION);
        database.migrateTo(Migration1460367000CreateIdentitiy.VERSION);
    }

    @Test
    public void stopsAtMaxMigration() throws Exception {
        database.migrateTo(0);
        assertFalse("too many migrations executed", database.tableExists("identity"));
    }

    @Test
    public void migratesAll() throws Exception {
        database.migrate();
        assertTrue(database.tableExists("identity"));
    }

    @Test
    public void rollsBackFailingMigrations() throws Exception {
        try {
            database.migrate(new FailingMigration(connection));
            fail("no exception thrown on failed migration");
        } catch (MigrationException ignored) {}

        assertFalse("partly executed migration not rolled back fully", tableExists("test1"));
    }
}
