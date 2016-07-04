package de.qabel.desktop.repository.sqlite.migration;

import de.qabel.core.repository.sqlite.DesktopClientDatabase;
import de.qabel.core.repository.sqlite.migration.AbstractMigration;

import java.sql.Connection;

public abstract class AbstractMigrationTest extends AbstractSqliteTest {
    protected AbstractMigration migration;

    @Override
    public void setUp() throws Exception {
        super.setUp();
        migration = createMigration(connection);
        new DesktopClientDatabase(connection).migrateTo(initialVersion());
    }

    public long initialVersion() {
        return migration.getVersion();
    }

    protected abstract AbstractMigration createMigration(Connection connection);
}
