package de.qabel.desktop.repository.sqlite;

import java.sql.PreparedStatement;
import java.sql.SQLException;

public interface ClientDatabase {
    int getVersion() throws SQLException;

    void migrate() throws MigrationException;

    void migrateTo(int toVersion) throws MigrationException;

    void migrate(int toVersion, int fromVersion) throws MigrationException;

    PreparedStatement prepare(String sql) throws SQLException;
}
