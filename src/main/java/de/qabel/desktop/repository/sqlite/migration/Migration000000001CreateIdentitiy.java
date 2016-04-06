package de.qabel.desktop.repository.sqlite.migration;

import java.sql.Connection;
import java.sql.SQLException;

public class Migration000000001CreateIdentitiy extends AbstractMigration {
    public static final int VERSION = 1;  // timestamp in UTC

    public Migration000000001CreateIdentitiy(Connection connection) {
        super(connection);
    }

    @Override
    public int getVersion() {
        return VERSION;
    }

    @Override
    public void up() throws SQLException {
        execute(
            "CREATE TABLE identity (" +
                "id INT UNSIGNED PRIMARY KEY," +
                "publicKey VARCHAR(64) NOT NULL," +
                "privateKey VARCHAR(64) NOT NULL," +
                "alias VARCHAR(255) NOT NULL," +
                "email VARCHAR(255) NULL," +
                "phone VARCHAR(255) NULL" +
            ")"
        );
        execute("CREATE INDEX idx_identity_publicKey ON identity (publicKey)");
        execute(
            "CREATE TABLE drop_url (" +
                "id INT UNSIGNED PRIMARY KEY," +
                "identity_id INT UNSIGNED NOT NULL," +
                "url VARCHAR(2000) NOT NULL," +
                "FOREIGN KEY (identity_id) REFERENCES identity (id)" +
            ")"
        );
    }

    @Override
    public void down() throws SQLException {
        execute("DROP TABLE drop_url");
        execute("DROP TABLE identity");
    }
}
