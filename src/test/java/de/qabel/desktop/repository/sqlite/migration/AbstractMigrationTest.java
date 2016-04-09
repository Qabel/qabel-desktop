package de.qabel.desktop.repository.sqlite.migration;

import de.qabel.desktop.repository.sqlite.DefaultClientDatabase;

import java.sql.Connection;
import java.sql.SQLException;

public abstract class AbstractMigrationTest extends AbstractSqliteTest {
    protected AbstractMigration migration;

    @Override
    public void setUp() throws Exception {
        super.setUp();
        migration = createMigration(connection);
        new DefaultClientDatabase(connection).migrateTo(migration.getVersion());
    }

    protected abstract AbstractMigration createMigration(Connection connection);
}
