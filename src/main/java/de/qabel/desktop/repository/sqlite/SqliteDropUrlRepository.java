package de.qabel.desktop.repository.sqlite;

import de.qabel.core.config.Identity;
import de.qabel.core.drop.DropURL;
import de.qabel.desktop.repository.exception.PersistenceException;

import java.util.Collection;

public class SqliteDropUrlRepository extends AbstractSqliteRepository<DropURL> {
    private static final String TABLE_NAME = "drop_url";

    public SqliteDropUrlRepository(ClientDatabase database, Hydrator<DropURL> hydrator) {
        super(database, hydrator, TABLE_NAME);
    }

    public Collection<DropURL> findAll(Identity identity) throws PersistenceException {
        return findAll("identity_id=?", identity.getId());
    }
}
