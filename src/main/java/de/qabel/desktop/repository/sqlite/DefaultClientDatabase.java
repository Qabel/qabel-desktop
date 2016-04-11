package de.qabel.desktop.repository.sqlite;

import de.qabel.desktop.repository.TransactionManager;
import de.qabel.desktop.repository.exception.PersistenceException;
import de.qabel.desktop.repository.sqlite.migration.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;

public class DefaultClientDatabase implements ClientDatabase {
    private static final Logger logger = LoggerFactory.getLogger(DefaultClientDatabase.class);
    private final Connection connection;
    private TransactionManager transactionManager;

    public DefaultClientDatabase(Connection connection) {
        this.connection = connection;
        transactionManager = new SqliteTransactionManager(connection);
    }

    public static AbstractMigration[] getMigrations(Connection connection) {
        return new AbstractMigration[]{
            new Migration000000001CreateIdentitiy(connection),
            new Migration000000002CreateContact(connection),
            new Migration000000003CreateAccount(connection),
            new Migration000000004ClientConfiguration(connection),
            new Migration000000005DropState(connection),
            new Migration000000006BoxSync(connection),
            new Migration000000007ShareNotification(connection)
        };
    }

    @Override
    public int getVersion() throws SQLException {
        try (Statement statement = connection.createStatement()) {
            try (ResultSet resultSet = statement.executeQuery("PRAGMA USER_VERSION")) {
                resultSet.next();
                return resultSet.getInt(1);
            }
        }
    }

    @Override
    public synchronized void migrateTo(int toVersion) throws MigrationException {
        try {
            migrate(toVersion, getVersion());
        } catch (SQLException e) {
            throw new MigrationException("failed to determine current version: " + e.getMessage(), e);
        }
    }

    @Override
    public void migrate(int toVersion, int fromVersion) throws MigrationException {
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

    public synchronized void setVersion(int version) throws SQLException {
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
        return connection.prepareStatement(sql);
    }

    public TransactionManager getTransactionManager() {
        return transactionManager;
    }
}