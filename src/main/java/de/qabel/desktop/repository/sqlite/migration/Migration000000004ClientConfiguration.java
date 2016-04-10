package de.qabel.desktop.repository.sqlite.migration;

import java.sql.Connection;
import java.sql.SQLException;

public class Migration000000004ClientConfiguration extends AbstractMigration {
    public Migration000000004ClientConfiguration(Connection connection) {
        super(connection);
    }

    @Override
    public int getVersion() {
        return 4;
    }

    @Override
    public void up() throws SQLException {
        execute(
            "CREATE TABLE client_configuration (" +
                "id INTEGER PRIMARY KEY," +
                "key VARCHAR(255) NOT NULL UNIQUE ON CONFLICT REPLACE," +
                "value VARCHAR(255) NULL" +
            ")"
        );
    }

    @Override
    public void down() throws SQLException {
        execute("DROP TABLE client_configuration");
    }
}
