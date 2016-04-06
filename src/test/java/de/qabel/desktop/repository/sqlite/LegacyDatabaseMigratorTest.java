package de.qabel.desktop.repository.sqlite;

import de.qabel.core.config.Identities;
import de.qabel.core.config.Identity;
import de.qabel.core.config.SQLitePersistence;
import de.qabel.desktop.config.factory.DefaultIdentityFactory;
import de.qabel.desktop.config.factory.DropUrlGenerator;
import de.qabel.desktop.config.factory.IdentityBuilder;
import de.qabel.desktop.repository.EntityManager;
import de.qabel.desktop.repository.persistence.PersistenceIdentityRepository;
import de.qabel.desktop.repository.sqlite.hydrator.IdentityHydrator;
import de.qabel.desktop.repository.sqlite.migration.AbstractSqliteTest;
import org.junit.Test;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

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

        LegacyDatabaseMigrator.migrate(persistence, database);
    }

    @Test
    public void migratesIdentities() throws Exception {
        PersistenceIdentityRepository legacyRepo = new PersistenceIdentityRepository(persistence);
        Identity identity = new IdentityBuilder(new DropUrlGenerator("http://localhost")).withAlias("tester").build();
        legacyRepo.save(identity);

        LegacyDatabaseMigrator.migrate(persistence, database);

        em = new EntityManager();
        Identities identities = getIdentityRepo().findAll();
        assertEquals(1, identities.getIdentities().size());
        Identity newIdentity = (Identity) identities.getIdentities().toArray()[0];
        assertEquals(identity.getKeyIdentifier(), newIdentity.getKeyIdentifier());
    }

    public SqliteIdentityRepository getIdentityRepo() {
        return new SqliteIdentityRepository(database, em);
    }
}
