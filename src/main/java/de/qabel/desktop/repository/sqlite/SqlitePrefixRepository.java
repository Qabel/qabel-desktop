package de.qabel.desktop.repository.sqlite;

import de.qabel.core.config.Identity;
import de.qabel.desktop.repository.exception.PersistenceException;
import de.qabel.desktop.repository.sqlite.hydrator.PrefixHydrator;

import java.util.Collection;

public class SqlitePrefixRepository extends AbstractSqliteRepository<String> {
    private static final String TABLE_NAME = "prefix";

    public SqlitePrefixRepository(ClientDatabase database) {
        super(database, new PrefixHydrator(), TABLE_NAME);
    }

    public Collection<String> findAll(Identity identity) throws PersistenceException {
        return findAll("identity_id=?", identity.getId());
    }
}
