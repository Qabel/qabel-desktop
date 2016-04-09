package de.qabel.desktop.repository.sqlite;

import de.qabel.core.config.Contact;
import de.qabel.core.config.Identity;
import de.qabel.core.drop.DropURL;
import de.qabel.desktop.repository.exception.PersistenceException;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Collection;

public class SqliteContactDropUrlRepository extends AbstractSqliteRepository<DropURL> {
    public static final String TABLE_NAME = "contact_drop_url";

    public SqliteContactDropUrlRepository(ClientDatabase database, Hydrator<DropURL> hydrator) {
        super(database, hydrator, TABLE_NAME);
    }

    public Collection<DropURL> findAll(Contact contact) throws PersistenceException {
        return findAll("contact_id=?", contact.getId());
    }

    public void delete(Contact contact) throws SQLException {
        try (PreparedStatement dropDrops = database.prepare(
            "DELETE FROM " + TABLE_NAME + " WHERE contact_id = ?"
        )) {
            dropDrops.setInt(1, contact.getId());
            dropDrops.execute();
        }
    }

    public void store(Contact contact) throws SQLException {
        try (PreparedStatement dropStatement = database.prepare(
            "INSERT INTO " + TABLE_NAME + " (contact_id, url) VALUES (?, ?)"
        )) {
            for (DropURL url : contact.getDropUrls()) {
                dropStatement.setInt(1, contact.getId());
                dropStatement.setString(2, url.toString());
                dropStatement.execute();
            }
        }
    }
}
