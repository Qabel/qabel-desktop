package de.qabel.desktop.repository.sqlite.factory;

import java.nio.file.Path;
import java.sql.Connection;
import java.sql.SQLException;

public class SqliteConnectionFactorySpy extends SqliteConnectionFactory {
    private Path dbFile;

    @Override
    public Connection getConnection(Path dbFile) throws SQLException {
        this.dbFile = dbFile;
        return super.getConnection(dbFile);
    }

    public Path getDbFile() {
        return dbFile;
    }
}
