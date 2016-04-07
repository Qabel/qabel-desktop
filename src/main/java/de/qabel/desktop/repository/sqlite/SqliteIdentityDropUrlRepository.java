package de.qabel.desktop.repository.sqlite;

import de.qabel.core.config.Identity;
import de.qabel.core.drop.DropURL;
import de.qabel.desktop.repository.exception.PersistenceException;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Collection;

public class SqliteIdentityDropUrlRepository extends AbstractSqliteRepository<DropURL> {
    public static final String TABLE_NAME = "identity_drop_url";

    public SqliteIdentityDropUrlRepository(ClientDatabase database, Hydrator<DropURL> hydrator) {
        super(database, hydrator, TABLE_NAME);
    }

    public Collection<DropURL> findAll(Identity identity) throws PersistenceException {
        return findAll("identity_id=?", identity.getId());
    }

    public void delete(Identity identity) throws SQLException {
        PreparedStatement dropDrops = database.prepare(
            "DELETE FROM " + TABLE_NAME + " WHERE identity_id = ?"
        );
        dropDrops.setInt(1, identity.getId());
        dropDrops.execute();
    }

    public void store(Identity identity) throws SQLException {
        for (DropURL url : identity.getDropUrls()) {
            PreparedStatement dropStatement = database.prepare(
                "INSERT INTO " + TABLE_NAME + " (identity_id, url) VALUES (?, ?)"
            );
            dropStatement.setInt(1, identity.getId());
            dropStatement.setString(2, url.toString());
            dropStatement.execute();
        }
    }
}
