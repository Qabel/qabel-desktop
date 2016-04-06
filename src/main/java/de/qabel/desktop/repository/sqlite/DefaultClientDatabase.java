package de.qabel.desktop.repository.sqlite;

import de.qabel.desktop.repository.sqlite.migration.AbstractMigration;
import de.qabel.desktop.repository.sqlite.migration.Migration000000001CreateIdentitiy;
import de.qabel.desktop.repository.sqlite.migration.MigrationFailedException;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class DefaultClientDatabase implements ClientDatabase {
    private Connection connection;

    public DefaultClientDatabase(Connection connection) {
        this.connection = connection;
    }

    public static AbstractMigration[] getMigrations(Connection connection) {
        return new AbstractMigration[]{
            new Migration000000001CreateIdentitiy(connection)
        };
    }

    @Override
    public int getVersion() throws SQLException {
        ResultSet resultSet = connection.createStatement().executeQuery("PRAGMA USER_VERSION");
        resultSet.next();
        return resultSet.getInt(1);
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
            migration.up();
            setVersion(migration.getVersion());
        } catch (SQLException e) {
            throw new MigrationFailedException(migration, e.getMessage(), e);
        }
    }

    public synchronized void setVersion(int version) throws SQLException {
        connection.createStatement().execute("PRAGMA USER_VERSION = " + version);
    }

    public boolean tableExists(String tableName) throws SQLException {
        PreparedStatement statement = connection.prepareStatement(
            "SELECT COUNT(*) FROM sqlite_master WHERE type='table' AND name=?"
        );
        statement.setString(1, tableName);
        statement.execute();
        ResultSet rs = statement.getResultSet();
        rs.next();
        return rs.getInt(1) > 0;
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
}
