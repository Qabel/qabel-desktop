package de.qabel.desktop.repository.sqlite;

import de.qabel.core.config.*;
import de.qabel.core.crypto.QblECPublicKey;
import de.qabel.core.drop.DropMessage;
import de.qabel.core.drop.DropURL;
import de.qabel.desktop.config.*;
import de.qabel.desktop.config.factory.ClientConfigurationFactory;
import de.qabel.desktop.config.factory.DropUrlGenerator;
import de.qabel.desktop.config.factory.IdentityBuilder;
import de.qabel.desktop.daemon.drop.ShareNotificationMessage;
import de.qabel.desktop.nio.boxfs.BoxFileSystem;
import de.qabel.desktop.repository.BoxSyncRepository;
import de.qabel.desktop.repository.EntityManager;
import de.qabel.desktop.repository.persistence.*;
import de.qabel.desktop.repository.sqlite.migration.AbstractSqliteTest;
import de.qabel.desktop.ui.actionlog.PersistenceDropMessage;
import org.junit.Test;
import org.spongycastle.util.encoders.Hex;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Date;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

public class LegacyDatabaseMigratorTest extends AbstractSqliteTest {
    private SQLitePersistence persistence;
    private Path legacyFile;
    private ClientDatabase database;
    private EntityManager em = new EntityManager();
    private String key;

    @Override
    public void setUp() throws Exception {
        super.setUp();
        legacyFile = Files.createTempFile("qabel", "legacydb");
        persistence = new SQLitePersistence(legacyFile.toAbsolutePath().toString());
        database = new DesktopClientDatabase(connection);
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
        PersistenceIdentityRepository legacyRepo = getLegacyIdentityRepo();
        Identity identity = new IdentityBuilder(new DropUrlGenerator("http://localhost")).withAlias("tester").build();
        legacyRepo.save(identity);

        migrate(persistence);

        Identities identities = getIdentityRepo().findAll();
        assertEquals(1, identities.getIdentities().size());
        Identity newIdentity = (Identity) identities.getIdentities().toArray()[0];
        assertEquals(identity.getKeyIdentifier(), newIdentity.getKeyIdentifier());
    }

    @Test
    public void migratesContacts() throws Exception {
        PersistenceContactRepository legacyRepo = getLegacyContactRepo();
        PersistenceIdentityRepository legacyIdentityRepo = getLegacyIdentityRepo();
        Identity identity = new IdentityBuilder(new DropUrlGenerator("http://localhost")).withAlias("tester").build();
        DropURL dropUrl = new DropUrlGenerator("http://localhost").generateUrl();
        List<DropURL> urls = new LinkedList<>();
        urls.add(dropUrl);
        Contact contact = new Contact("test", urls, new QblECPublicKey("contactPub".getBytes()));
        legacyIdentityRepo.save(identity);
        legacyRepo.find(identity);    // indeed required for the legacy repo
        legacyRepo.save(contact, identity);

        migrate(persistence);

        identity = getIdentityRepo().find(identity.getKeyIdentifier());
        Contacts contacts = getContactRepo().find(identity);
        assertEquals(1, contacts.getContacts().size());
        assertEquals(contact.getKeyIdentifier(), contacts.getContacts().toArray(new Contact[1])[0].getKeyIdentifier());
    }

    private PersistenceContactRepository getLegacyContactRepo() {
        return new PersistenceContactRepository(persistence);
    }

    @Test
    public void migratesAccounts() throws Exception {
        PersistenceAccountRepository legacyRepo = getLegacyAccountRepo();
        Account account = new Account("a", "b", "c");
        legacyRepo.save(account);

        migrate(persistence);

        List<Account> accounts = getAccountRepo().findAll();
        assertEquals(1, accounts.size());
        Account newAccount = accounts.get(0);
        assertEquals("a", newAccount.getProvider());
        assertEquals("b", newAccount.getUser());
        assertEquals("c", newAccount.getAuth());
    }

    @Test
    public void migratesClientConfig() throws Exception {
        // arrange
        PersistenceIdentityRepository legacyIdentityRepo = getLegacyIdentityRepo();
        PersistenceAccountRepository legacyAccountRepo = getLegacyAccountRepo();
        PersistenceClientConfigurationRepository legacyRepo = new PersistenceClientConfigurationRepository(
            persistence,
            new ClientConfigurationFactory(),
            legacyIdentityRepo,
            legacyAccountRepo
        );
        Account account = new Account("a", "b", "c");
        Identity identity = new IdentityBuilder(new DropUrlGenerator("http://localhost")).withAlias("tester").build();
        legacyIdentityRepo.save(identity);
        ClientConfiguration clientConfiguration = new DefaultClientConfiguration();
        clientConfiguration.setAccount(account);
        legacyAccountRepo.save(account);
        clientConfiguration.selectIdentity(identity);
        clientConfiguration.setDeviceId("deviceId001");
        clientConfiguration.setLastDropPoll(identity, new Date(12345678L));

        key = Hex.toHexString("key".getBytes());
        ShareNotificationMessage message = new ShareNotificationMessage("url", key, "message");
        clientConfiguration.getShareNotification(identity).add(message);

        Path localPath = Paths.get("/tmp/local");
        Path remotePath = BoxFileSystem.getRoot().resolve("tmp").resolve("remote");
        BoxSyncConfig boxSyncConfig = new DefaultBoxSyncConfig("name", localPath, remotePath, identity, account);
        boxSyncConfig.pause();
        clientConfiguration.getBoxSyncConfigs().add(boxSyncConfig);

        legacyRepo.save(clientConfiguration);

        // act
        migrate(persistence);

        // assert
        SqliteIdentityRepository identityRepo = new SqliteIdentityRepository(database, em);
        ClientConfig config = new RepositoryBasedClientConfig(
            new SqliteClientConfigRepository(database),
            new SqliteAccountRepository(database, em),
            identityRepo,
            new SqliteDropStateRepository(database),
            new SqliteShareNotificationRepository(database, em)
        );
        identity = identityRepo.find(identity.getKeyIdentifier());
        assertEquals(identity.getKeyIdentifier(), config.getSelectedIdentity().getKeyIdentifier());
        assertEquals(account.getProvider(), config.getAccount().getProvider());
        assertEquals(account.getUser(), config.getAccount().getUser());
        assertEquals("deviceId001", config.getDeviceId());
        assertEquals(12345678L, config.getLastDropPoll(identity).getTime());

        List<ShareNotificationMessage> notifications = config.getShareNotification(identity).getNotifications();
        assertEquals(1, notifications.size());
        assertEquals("url", notifications.get(0).getUrl());

        BoxSyncRepository syncRepo = new SqliteBoxSyncRepository(database, em);
        List<BoxSyncConfig> syncConfigs = syncRepo.findAll();
        assertEquals(1, syncConfigs.size());
        BoxSyncConfig syncConfig = syncConfigs.get(0);
        assertEquals(Paths.get("/tmp/local").toString(), syncConfig.getLocalPath().toString());
        assertEquals("/tmp/remote", syncConfig.getRemotePath().toString());
        assertEquals("name", syncConfig.getName());
        assertEquals(identity.getKeyIdentifier(), syncConfig.getIdentity().getKeyIdentifier());
        assertEquals(account.getUser(), syncConfig.getAccount().getUser());
        assertFalse(syncConfig.isPaused());
    }

    private PersistenceAccountRepository getLegacyAccountRepo() {
        return new PersistenceAccountRepository(persistence);
    }

    private PersistenceIdentityRepository getLegacyIdentityRepo() {
        return new PersistenceIdentityRepository(persistence);
    }

    @Test
    public void migratesMessages() throws Exception {
        Identity legacyIdentity = new IdentityBuilder(new DropUrlGenerator("http://localhost"))
            .withAlias("tester").build();
        Contact legacyContact = new Contact("someone", new HashSet<>(), new QblECPublicKey("nokey".getBytes()));
        getLegacyIdentityRepo().save(legacyIdentity);
        getLegacyContactRepo().save(legacyContact, legacyIdentity);

        DropMessage legacyDropMessage = new DropMessage(legacyContact, "stuff", "ordinary message");
        PersistenceDropMessageRepository legacyRepo = new PersistenceDropMessageRepository(persistence);
        legacyRepo.addMessage(legacyDropMessage, legacyContact, legacyIdentity, false);

        migrate(persistence);

        Identity identity = getIdentityRepo().find(legacyIdentity.getKeyIdentifier());
        Contact contact = getContactRepo().findByKeyId(identity, legacyContact.getKeyIdentifier());
        List<PersistenceDropMessage> messages = getDropMessageRepo().loadConversation(contact, identity);
        assertEquals(1, messages.size());
        PersistenceDropMessage message = messages.get(0);
        assertEquals("stuff", message.getDropMessage().getDropPayload());
        assertEquals("ordinary message", message.getDropMessage().getDropPayloadType());
        assertFalse(message.isSent());
        assertFalse(message.isSeen());
        assertEquals(
            legacyDropMessage.getCreationDate().getTime(),
            message.getDropMessage().getCreationDate().getTime()
        );
    }

    public SqliteIdentityRepository getIdentityRepo() {
        return new SqliteIdentityRepository(database, em);
    }

    public SqliteContactRepository getContactRepo() {
        return new SqliteContactRepository(database, em);
    }

    public SqliteAccountRepository getAccountRepo() {
        return new SqliteAccountRepository(database, em);
    }

    public SqliteDropMessageRepository getDropMessageRepo() {
        return new SqliteDropMessageRepository(database, em);
    }
}
