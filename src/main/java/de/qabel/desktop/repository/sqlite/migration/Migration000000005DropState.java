package de.qabel.desktop.repository.sqlite.migration;

import java.sql.Connection;
import java.sql.SQLException;

public class Migration000000005DropState extends AbstractMigration {
    public Migration000000005DropState(Connection connection) {
        super(connection);
    }

    @Override
    public int getVersion() {
        return 5;
    }

    @Override
    public void up() throws SQLException {
        execute(
            "CREATE TABLE drop_state (" +
                "id INTEGER PRIMARY KEY," +
                "`drop` VARCHAR(255) NOT NULL UNIQUE ON CONFLICT REPLACE," +
                "`last_request_stamp` VARCHAR(255) NOT NULL" +
            ")"
        );
    }

    @Override
    public void down() throws SQLException {
        execute("DROP TABLE drop_state");
    }
}
