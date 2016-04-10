package de.qabel.desktop.repository.sqlite.migration;

import java.sql.Connection;
import java.sql.SQLException;

public class Migration000000006BoxSync extends AbstractMigration {
    public Migration000000006BoxSync(Connection connection) {
        super(connection);
    }

    @Override
    public int getVersion() {
        return 6;
    }

    @Override
    public void up() throws SQLException {
        execute(
            "CREATE TABLE box_sync (" +
                "id INTEGER PRIMARY KEY," +
                "name VARCHAR(255) NOT NULL," +
                "account_id INTEGER NOT NULL," +
                "identity_id INTEGER NOT NULL," +
                "local_path TEXT NOT NULL UNIQUE," +
                "remote_path TEXT NOT NULL," +
                "paused BOOLEAN NOT NULL DEFAULT false," +
                "FOREIGN KEY (identity_id) REFERENCES identity(id) ON DELETE CASCADE," +
                "FOREIGN KEY (account_id) REFERENCES account(id) ON DELETE CASCADE" +
            ")"
        );
    }

    @Override
    public void down() throws SQLException {
        execute("DROP TABLE box_sync");
    }
}
