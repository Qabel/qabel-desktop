package de.qabel.desktop.repository.sqlite;

import de.qabel.core.config.*;
import de.qabel.core.drop.DropMessage;
import de.qabel.desktop.config.BoxSyncConfig;
import de.qabel.desktop.config.ClientConfig;
import de.qabel.desktop.config.ClientConfiguration;
import de.qabel.desktop.config.RepositoryBasedClientConfig;
import de.qabel.desktop.config.factory.*;
import de.qabel.desktop.daemon.drop.ShareNotificationMessage;
import de.qabel.desktop.repository.EntityManager;
import de.qabel.desktop.repository.exception.EntityNotFoundException;
import de.qabel.desktop.repository.exception.PersistenceException;
import de.qabel.desktop.repository.persistence.*;
import de.qabel.desktop.repository.sqlite.hydrator.*;
import de.qabel.desktop.ui.actionlog.PersistenceDropMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Field;
import java.util.Date;
import java.util.List;

public class LegacyDatabaseMigrator {
    private static final Logger logger = LoggerFactory.getLogger(LegacyDatabaseMigrator.class);
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

    private final SqliteDropUrlRepository dropUrlRepo;
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
    private final PersistenceDropMessageRepository legacyDropMessageRepo;
    private final SqliteDropMessageRepository dropMessageRepo;

    public LegacyDatabaseMigrator(SQLitePersistence source, ClientDatabase target) {
        this.source = source;
        this.target = target;

        legacyIdentityRepo = new PersistenceIdentityRepository(source);
        prefixRepository = new SqlitePrefixRepository(target);
        dropUrlRepo = new SqliteDropUrlRepository(target, dropURLHydrator);
        identityHydrator = new IdentityHydrator(identityFactory, em, dropUrlRepo, prefixRepository);
        identityRepo = new SqliteIdentityRepository(target, identityHydrator, dropUrlRepo, prefixRepository);

        contactHydrator = new ContactHydrator(em, contactFactory, dropUrlRepo);
        contactRepo = new SqliteContactRepository(target, contactHydrator, dropUrlRepo);
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

        legacyDropMessageRepo = new PersistenceDropMessageRepository(source);
        dropMessageRepo = new SqliteDropMessageRepository(target, em);
    }

    public synchronized void migrate() throws MigrationException {
        try {
            migrateIdentities();
            migrateContacts();
            migrateAccounts();
            migrateClientConfig();
            migrateMessages();
        } catch (Exception e) {
            throw new MigrationException("Failed to migrate legacy database", e);
        }
    }

    private void migrateContacts() throws EntityNotFoundException, PersistenceException {
        for (Identity identity : legacyIdentityRepo.findAll().getIdentities()) {
            Identity newIdentity = identityRepo.find(identity.getKeyIdentifier());
            for (Contact contact : legacyContactRepo.find(identity).getContacts()) {
                contactRepo.save(contact, newIdentity);
            }
        }
    }

    private void migrateIdentities() throws EntityNotFoundException, PersistenceException {
        for (Identity identity : legacyIdentityRepo.findAll().getIdentities()) {
            identityRepo.save(identity);
        }
    }

    private void migrateAccounts() throws EntityNotFoundException, PersistenceException {
        for (Account account : legacyAccountRepo.findAll()) {
            accountRepo.save(account);
        }
    }

    private void migrateMessages() throws EntityNotFoundException, PersistenceException {
        for (Identity identity : legacyIdentityRepo.findAll().getIdentities()) {
            Identity newIdentity = identityRepo.find(identity.getKeyIdentifier());
            for (Contact contact : legacyContactRepo.find(identity).getContacts()) {
                Contact newContact = contactRepo.findByKeyId(identity, contact.getKeyIdentifier());
                List<PersistenceDropMessage> messages = legacyDropMessageRepo.loadConversation(contact, identity);

                for (PersistenceDropMessage legacyMessage : messages) {
                    DropMessage dropMessage = new DropMessage(
                        legacyMessage.isSent() ? newIdentity : newContact,
                        legacyMessage.getDropMessage().getDropPayload(),
                        legacyMessage.getDropMessage().getDropPayloadType()
                    );
                    try {
                        Field field = DropMessage.class.getDeclaredField("created");
                        field.setAccessible(true);
                        field.set(dropMessage, legacyMessage.getDropMessage().getCreationDate());
                    } catch (IllegalAccessException | NoSuchFieldException e) {
                        logger.warn("failed to preserve message date: " + e.getMessage(), e);
                    }
                    PersistenceDropMessage message = new PersistenceDropMessage(
                        dropMessage,
                        legacyMessage.isSent() ? newIdentity : newContact,
                        legacyMessage.isSent() ? newContact : newIdentity,
                        legacyMessage.isSent(),
                        legacyMessage.isSeen()
                    );
                    dropMessageRepo.save(message);
                }
            }
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
