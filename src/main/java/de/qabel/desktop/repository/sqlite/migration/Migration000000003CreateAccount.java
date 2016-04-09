package de.qabel.desktop.repository.sqlite.migration;

import java.sql.Connection;
import java.sql.SQLException;

public class Migration000000003CreateAccount extends AbstractMigration {
    public Migration000000003CreateAccount(Connection connection) {
        super(connection);
    }

    @Override
    public int getVersion() {
        return 3;
    }

    @Override
    public void up() throws SQLException {
        execute(
            "CREATE TABLE account (" +
                "id INTEGER PRIMARY KEY," +
                "provider VARCHAR(2000) NOT NULL," +
                "user VARCHAR(255) NOT NULL," +
                "auth VARCHAR(255) NOT NULL," +
                "UNIQUE (provider, user) ON CONFLICT REPLACE" +
            ")"
        );
    }

    @Override
    public void down() throws SQLException {
        execute("DROP TABLE account");
    }
}
