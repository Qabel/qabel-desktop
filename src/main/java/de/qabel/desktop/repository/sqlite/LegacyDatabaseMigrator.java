package de.qabel.desktop.repository.sqlite;

import de.qabel.core.config.*;
import de.qabel.desktop.config.BoxSyncConfig;
import de.qabel.desktop.config.ClientConfig;
import de.qabel.desktop.config.ClientConfiguration;
import de.qabel.desktop.config.RepositoryBasedClientConfig;
import de.qabel.desktop.config.factory.*;
import de.qabel.desktop.daemon.drop.ShareNotificationMessage;
import de.qabel.desktop.repository.EntityManager;
import de.qabel.desktop.repository.exception.EntityNotFoundExcepion;
import de.qabel.desktop.repository.exception.PersistenceException;
import de.qabel.desktop.repository.persistence.PersistenceAccountRepository;
import de.qabel.desktop.repository.persistence.PersistenceClientConfigurationRepository;
import de.qabel.desktop.repository.persistence.PersistenceContactRepository;
import de.qabel.desktop.repository.persistence.PersistenceIdentityRepository;
import de.qabel.desktop.repository.sqlite.hydrator.*;

import java.util.Date;

public class LegacyDatabaseMigrator {
    private final SQLitePersistence source;
    private final ClientDatabase target;
    private final EntityManager em = new EntityManager();

    private final DropURLHydrator dropURLHydrator = new DropURLHydrator();
    private final PrefixHydrator prefixHydrator = new PrefixHydrator();
    private final IdentityHydrator identityHydrator;
    private final IdentityFactory identityFactory = new DefaultIdentityFactory();
    private final ContactFactory contactFactory = new DefaultContactFactory();
    private final AccountFactory accountFactory = new DefaultAccountFactory();
    private final ContactHydrator contactHydrator;
    private final AccountHydrator accountHydrator = new AccountHydrator(em, accountFactory);
    private final ClientConfigurationFactory legacyConfigFactory = new ClientConfigurationFactory();

    private final SqliteIdentityDropUrlRepository identityDropUrlRepo;
    private final SqliteContactDropUrlRepository contactDropUrlRepo;
    private final SqlitePrefixRepository prefixRepository;
    private final PersistenceIdentityRepository legacyIdentityRepo;
    private final SqliteIdentityRepository identityRepo;
    private final SqliteContactRepository contactRepo;
    private final PersistenceContactRepository legacyContactRepo;
    private final PersistenceAccountRepository legacyAccountRepo;
    private final SqliteAccountRepository accountRepo;
    private final PersistenceClientConfigurationRepository legacyClientConfigRepo;
    private final SqliteClientConfigRepository clientConfigRepo;
    private final ClientConfig config;
    private final SqliteDropStateRepository dropStateRepo;
    private final SqliteShareNotificationRepository shareNotificationRepo;
    private final SqliteBoxSyncRepository boxSyncRepo;

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

        legacyAccountRepo = new PersistenceAccountRepository(source);
        accountRepo = new SqliteAccountRepository(target, accountHydrator);

        legacyClientConfigRepo = new PersistenceClientConfigurationRepository(
            source,
            legacyConfigFactory,
            legacyIdentityRepo,
            legacyAccountRepo
        );
        clientConfigRepo = new SqliteClientConfigRepository(target);
        dropStateRepo = new SqliteDropStateRepository(target);
        shareNotificationRepo = new SqliteShareNotificationRepository(target, em);
        boxSyncRepo = new SqliteBoxSyncRepository(target, em);

        config = new RepositoryBasedClientConfig(
            clientConfigRepo,
            accountRepo,
            identityRepo,
            dropStateRepo,
            shareNotificationRepo
        );
    }

    public synchronized void migrate() throws MigrationException {
        try {
            migrateIdentities();
            migrateContacts();
            migrateAccounts();
            migrateClientConfig();
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

    private void migrateAccounts() throws EntityNotFoundExcepion, PersistenceException {
        for (Account account : legacyAccountRepo.findAll()) {
            accountRepo.save(account);
        }
    }

    private void migrateClientConfig() throws Exception {
        ClientConfiguration legacyConfig = legacyClientConfigRepo.load();
        Identity selectedIdentity = legacyConfig.getSelectedIdentity();
        if (selectedIdentity != null) {
            config.selectIdentity(selectedIdentity);
        }
        Account account = legacyConfig.getAccount();
        if (account != null) {
            config.setAccount(account);
        }
        config.setDeviceId(legacyConfig.getDeviceId());

        for (Identity identity : legacyIdentityRepo.findAll().getIdentities()) {
            Date lastPoll = legacyConfig.getLastDropPoll(identity);
            if (lastPoll == null) {
                continue;
            }
            config.setLastDropPoll(identity, lastPoll);

            for (ShareNotificationMessage message : legacyConfig.getShareNotification(identity).getNotifications()) {
                shareNotificationRepo.save(identity, message);
            }
        }

        for (BoxSyncConfig boxSyncConfig : legacyConfig.getBoxSyncConfigs()) {
            boxSyncRepo.save(boxSyncConfig);
        }
    }
}
