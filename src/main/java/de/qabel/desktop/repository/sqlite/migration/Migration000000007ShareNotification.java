package de.qabel.desktop.repository.sqlite.migration;

import java.sql.Connection;
import java.sql.SQLException;

public class Migration000000007ShareNotification extends AbstractMigration {
    public Migration000000007ShareNotification(Connection connection) {
        super(connection);
    }

    @Override
    public int getVersion() {
        return 7;
    }

    @Override
    public void up() throws SQLException {
        execute(
            "CREATE TABLE share_notification (" +
                "id INTEGER PRIMARY KEY," +
                "identity_id INTEGER NOT NULL," +
                "url VARCHAR(2000) NOT NULL," +
                "key VARCHAR(64) NOT NULL," +
                "message TEXT NULL," +
                "FOREIGN KEY (identity_id) REFERENCES identity(id) ON DELETE CASCADE" +
            ")"
        );
    }

    @Override
    public void down() throws SQLException {
        execute("DROP TABLE share_notification");
    }
}
