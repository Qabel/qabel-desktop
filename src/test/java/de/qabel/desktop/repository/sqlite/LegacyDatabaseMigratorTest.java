package de.qabel.desktop.repository.sqlite;

import de.qabel.core.config.*;
import de.qabel.core.crypto.QblECPublicKey;
import de.qabel.core.drop.DropURL;
import de.qabel.desktop.config.factory.DefaultIdentityFactory;
import de.qabel.desktop.config.factory.DropUrlGenerator;
import de.qabel.desktop.config.factory.IdentityBuilder;
import de.qabel.desktop.repository.EntityManager;
import de.qabel.desktop.repository.persistence.PersistenceContactRepository;
import de.qabel.desktop.repository.persistence.PersistenceIdentityRepository;
import de.qabel.desktop.repository.sqlite.hydrator.IdentityHydrator;
import de.qabel.desktop.repository.sqlite.migration.AbstractSqliteTest;
import org.junit.Test;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.LinkedList;
import java.util.List;

import static org.junit.Assert.assertEquals;

public class LegacyDatabaseMigratorTest extends AbstractSqliteTest {
    private SQLitePersistence persistence;
    private Path legacyFile;
    private ClientDatabase database;
    private EntityManager em;

    @Override
    public void setUp() throws Exception {
        super.setUp();
        legacyFile = Files.createTempFile("qabel", "legacydb");
        persistence = new SQLitePersistence(legacyFile.toAbsolutePath().toString());
        database = new DefaultClientDatabase(connection);
        database.migrate();
    }

    @Override
    public void tearDown() throws Exception {
        super.tearDown();
        Files.delete(legacyFile);
    }

    @Test
    public void migratesExistingDatabaseWithoutErrors() throws Exception {
        Path tmpFile = Files.createTempFile("qabel", "legacydb");
        Files.delete(tmpFile);
        Path existingDb = Paths.get(LegacyDatabaseMigratorTest.class.getResource("/db_legacy.sqlite").toURI());
        Files.copy(existingDb, tmpFile);
        SQLitePersistence persistence = new SQLitePersistence(tmpFile.toAbsolutePath().toString());

        migrate(persistence);
    }

    public void migrate(SQLitePersistence persistence) throws MigrationException {
        new LegacyDatabaseMigrator(persistence, database).migrate();
    }

    @Test
    public void migratesIdentities() throws Exception {
        PersistenceIdentityRepository legacyRepo = new PersistenceIdentityRepository(persistence);
        Identity identity = new IdentityBuilder(new DropUrlGenerator("http://localhost")).withAlias("tester").build();
        legacyRepo.save(identity);

        migrate(persistence);

        em = new EntityManager();
        Identities identities = getIdentityRepo().findAll();
        assertEquals(1, identities.getIdentities().size());
        Identity newIdentity = (Identity) identities.getIdentities().toArray()[0];
        assertEquals(identity.getKeyIdentifier(), newIdentity.getKeyIdentifier());
    }

    @Test
    public void migratesContacts() throws Exception {
        PersistenceContactRepository legacyRepo = new PersistenceContactRepository(persistence);
        PersistenceIdentityRepository legacyIdentityRepo = new PersistenceIdentityRepository(persistence);
        Identity identity = new IdentityBuilder(new DropUrlGenerator("http://localhost")).withAlias("tester").build();
        DropURL dropUrl = new DropUrlGenerator("http://localhost").generateUrl();
        List<DropURL> urls = new LinkedList<>();
        urls.add(dropUrl);
        Contact contact = new Contact("test", urls, new QblECPublicKey("contactPub".getBytes()));
        legacyIdentityRepo.save(identity);
        legacyRepo.find(identity);    // indeed required for the legacy repo
        legacyRepo.save(contact, identity);

        migrate(persistence);

        em = new EntityManager();
        identity = getIdentityRepo().find(identity.getKeyIdentifier());
        Contacts contacts = getContactRepo().find(identity);
        assertEquals(1, contacts.getContacts().size());
        assertEquals(contact.getKeyIdentifier(), contacts.getContacts().toArray(new Contact[1])[0].getKeyIdentifier());
    }

    public SqliteIdentityRepository getIdentityRepo() {
        return new SqliteIdentityRepository(database, em);
    }

    public SqliteContactRepository getContactRepo() {
        return new SqliteContactRepository(database, em);
    }
}
