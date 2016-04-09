package de.qabel.desktop.repository.sqlite;

import de.qabel.core.config.Contact;
import de.qabel.core.config.Contacts;
import de.qabel.core.config.Identity;
import de.qabel.core.config.SQLitePersistence;
import de.qabel.desktop.config.factory.ContactFactory;
import de.qabel.desktop.config.factory.DefaultContactFactory;
import de.qabel.desktop.config.factory.DefaultIdentityFactory;
import de.qabel.desktop.config.factory.IdentityFactory;
import de.qabel.desktop.repository.EntityManager;
import de.qabel.desktop.repository.exception.EntityNotFoundExcepion;
import de.qabel.desktop.repository.exception.PersistenceException;
import de.qabel.desktop.repository.persistence.PersistenceContactRepository;
import de.qabel.desktop.repository.persistence.PersistenceIdentityRepository;
import de.qabel.desktop.repository.sqlite.hydrator.ContactHydrator;
import de.qabel.desktop.repository.sqlite.hydrator.DropURLHydrator;
import de.qabel.desktop.repository.sqlite.hydrator.IdentityHydrator;
import de.qabel.desktop.repository.sqlite.hydrator.PrefixHydrator;

public class LegacyDatabaseMigrator {
    private final SQLitePersistence source;
    private final ClientDatabase target;
    private final EntityManager em = new EntityManager();

    private final DropURLHydrator dropURLHydrator = new DropURLHydrator();
    private final PrefixHydrator prefixHydrator = new PrefixHydrator();
    private final IdentityHydrator identityHydrator;
    private final IdentityFactory identityFactory = new DefaultIdentityFactory();
    private final ContactFactory contactFactory = new DefaultContactFactory();
    private final ContactHydrator contactHydrator;

    private final SqliteIdentityDropUrlRepository identityDropUrlRepo;
    private final SqliteContactDropUrlRepository contactDropUrlRepo;
    private final SqlitePrefixRepository prefixRepository;
    private final PersistenceIdentityRepository legacyIdentityRepo;
    private final SqliteIdentityRepository identityRepo;
    private final SqliteContactRepository contactRepo;
    private final PersistenceContactRepository legacyContactRepo;

    public LegacyDatabaseMigrator(SQLitePersistence source, ClientDatabase target) {
        this.source = source;
        this.target = target;

        legacyIdentityRepo = new PersistenceIdentityRepository(source);
        identityDropUrlRepo = new SqliteIdentityDropUrlRepository(target, dropURLHydrator);
        prefixRepository = new SqlitePrefixRepository(target);
        identityHydrator = new IdentityHydrator(identityFactory, em, identityDropUrlRepo, prefixRepository);
        identityRepo = new SqliteIdentityRepository(target, identityHydrator, identityDropUrlRepo, prefixRepository);

        contactDropUrlRepo = new SqliteContactDropUrlRepository(target, dropURLHydrator);
        contactHydrator = new ContactHydrator(em, contactFactory, contactDropUrlRepo);
        contactRepo = new SqliteContactRepository(target, contactHydrator, contactDropUrlRepo);
        legacyContactRepo = new PersistenceContactRepository(source);
    }

    public synchronized void migrate() throws MigrationException {
        try {
            migrateIdentities();
            migrateContacts();
        } catch (Exception e) {
            throw new MigrationException("Failed to migrate legacy database", e);
        }
    }

    private void migrateContacts() throws EntityNotFoundExcepion, PersistenceException {
        for (Identity identity : legacyIdentityRepo.findAll().getIdentities()) {
            Identity newIdentity = identityRepo.find(identity.getKeyIdentifier());
            for (Contact contact : legacyContactRepo.find(identity).getContacts()) {
                contactRepo.save(contact, newIdentity);
            }
        }
    }

    private void migrateIdentities() throws EntityNotFoundExcepion, PersistenceException {

        for (Identity identity : legacyIdentityRepo.findAll().getIdentities()) {
            identityRepo.save(identity);
        }
    }
}
