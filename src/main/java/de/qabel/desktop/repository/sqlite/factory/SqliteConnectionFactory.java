package de.qabel.desktop.repository.sqlite.factory;

import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class SqliteConnectionFactory {
    public Connection getConnection(Path dbFile) throws SQLException {
        return DriverManager.getConnection("jdbc:sqlite://" + dbFile.toAbsolutePath());
    }
}
