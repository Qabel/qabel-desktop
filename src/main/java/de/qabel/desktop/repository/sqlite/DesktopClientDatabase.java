package de.qabel.desktop.repository.sqlite;

import de.qabel.desktop.repository.TransactionManager;
import de.qabel.desktop.repository.exception.PersistenceException;
import de.qabel.desktop.repository.sqlite.migration.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;

public class DesktopClientDatabase implements ClientDatabase {
    private static final Logger logger = LoggerFactory.getLogger(DesktopClientDatabase.class);
    private final Connection connection;
    private TransactionManager transactionManager;

    public DesktopClientDatabase(Connection connection) {
        this.connection = connection;
        transactionManager = new SqliteTransactionManager(connection);
    }

    public static AbstractMigration[] getMigrations(Connection connection) {
        return new AbstractMigration[]{
            new Migration1460367000CreateIdentitiy(connection),
            new Migration1460367005CreateContact(connection),
            new Migration1460367010CreateAccount(connection),
            new Migration1460367015ClientConfiguration(connection),
            new Migration1460367020DropState(connection),
            new Migration1460367025BoxSync(connection),
            new Migration1460367030ShareNotification(connection),
            new Migration1460367035Entity(connection),
            new Migration1460367040DropMessage(connection),
            new Migration1460987825PreventDuplicateContacts(connection)
        };
    }

    @Override
    public long getVersion() throws SQLException {
        try (Statement statement = connection.createStatement()) {
            try (ResultSet resultSet = statement.executeQuery("PRAGMA USER_VERSION")) {
                resultSet.next();
                return resultSet.getInt(1);
            }
        }
    }

    @Override
    public synchronized void migrateTo(long toVersion) throws MigrationException {
        try {
            migrate(toVersion, getVersion());
        } catch (SQLException e) {
            throw new MigrationException("failed to determine current version: " + e.getMessage(), e);
        }
    }

    @Override
    public void migrate(long toVersion, long fromVersion) throws MigrationException {
        for (AbstractMigration migration : getMigrations(connection)) {
            if (migration.getVersion() <= fromVersion) {
                continue;
            }
            if (migration.getVersion() > toVersion) {
                break;
            }

            migrate(migration);
        }
    }

    public void migrate(AbstractMigration migration) throws MigrationException {
        try {
            getTransactionManager().transactional(() -> {
                logger.info("Migrating " + migration.getClass().getSimpleName());
                migration.up();
                setVersion(migration.getVersion());
                logger.info("ClientDatabase now on version " + getVersion());
            });
        } catch (PersistenceException e) {
            throw new MigrationFailedException(migration, e.getMessage(), e);
        }
    }

    public synchronized void setVersion(long version) throws SQLException {
        try (Statement statement = connection.createStatement()) {
            statement.execute("PRAGMA USER_VERSION = " + version);
        }
    }

    public boolean tableExists(String tableName) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(
            "SELECT COUNT(*) FROM sqlite_master WHERE type='table' AND name=?"
        )) {
            statement.setString(1, tableName);
            statement.execute();
            try (ResultSet rs = statement.getResultSet()) {
                rs.next();
                return rs.getInt(1) > 0;
            }
        }
    }

    @Override
    public void migrate() throws MigrationException {
        AbstractMigration[] migrations = getMigrations(connection);
        migrateTo(migrations[migrations.length - 1].getVersion());
    }

    @Override
    public PreparedStatement prepare(String sql) throws SQLException {
        logger.trace(sql);
        return connection.prepareStatement(sql);
    }

    public TransactionManager getTransactionManager() {
        return transactionManager;
    }
}
