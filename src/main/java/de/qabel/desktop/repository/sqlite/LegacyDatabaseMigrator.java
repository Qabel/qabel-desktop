package de.qabel.desktop.repository.sqlite;

import de.qabel.core.config.Identity;
import de.qabel.core.config.SQLitePersistence;
import de.qabel.desktop.repository.EntityManager;
import de.qabel.desktop.repository.exception.EntityNotFoundExcepion;
import de.qabel.desktop.repository.exception.PersistenceException;
import de.qabel.desktop.repository.persistence.PersistenceIdentityRepository;

public class LegacyDatabaseMigrator {
    public static void migrate(SQLitePersistence source, ClientDatabase target) throws MigrationException {
        try {
            EntityManager em = new EntityManager();
            migrateIdentities(source, target, em);
        } catch (Exception e) {
            throw new MigrationException("Failed to migrate legacy database", e);
        }
    }

    private static void migrateIdentities(SQLitePersistence source, ClientDatabase target, EntityManager em) throws EntityNotFoundExcepion, PersistenceException {
        PersistenceIdentityRepository legacyRepo = new PersistenceIdentityRepository(source);
        SqliteIdentityRepository newRepo = new SqliteIdentityRepository(target, em);

        for (Identity identity : legacyRepo.findAll().getIdentities()) {
            newRepo.save(identity);
        }
    }
}
