package de.qabel.desktop.inject;

import de.qabel.desktop.config.ClientConfig;
import de.qabel.desktop.config.RepositoryBasedClientConfig;
import de.qabel.desktop.config.factory.*;
import de.qabel.desktop.inject.config.RuntimeConfiguration;
import de.qabel.desktop.repository.*;
import de.qabel.desktop.repository.sqlite.*;
import de.qabel.desktop.repository.sqlite.hydrator.AccountHydrator;
import de.qabel.desktop.repository.sqlite.hydrator.ContactHydrator;
import de.qabel.desktop.repository.sqlite.hydrator.DropURLHydrator;
import de.qabel.desktop.repository.sqlite.hydrator.IdentityHydrator;

public class NewConfigDesktopServiceFactory extends StaticDesktopServiceFactory {
    public NewConfigDesktopServiceFactory(RuntimeConfiguration runtimeConfiguration) {
        super(runtimeConfiguration);
    }

    @Override
    public synchronized IdentityRepository getIdentityRepository() {
        if (identityRepository == null) {
            identityRepository = new SqliteIdentityRepository(
                runtimeConfiguration.getConfigDatabase(),
                getIdentityHydrator(),
                getIdentityDropUrlRepository(),
                getPrefixRepository()
            );
        }
        return identityRepository;
    }

    @Override
    public synchronized ContactRepository getContactRepository() {
        if (contactRepository == null) {
            contactRepository = new SqliteContactRepository(
                runtimeConfiguration.getConfigDatabase(),
                getContactHydrator(),
                getContactDropUrlRepository()
            );
        }
        return contactRepository;
    }

    @Override
    public synchronized AccountRepository getAccountRepository() {
        if (accountRepository == null) {
            accountRepository = new SqliteAccountRepository(
                runtimeConfiguration.getConfigDatabase(),
                getAccountHydrator()
            );
        }
        return accountRepository;
    }

    @Override
    public synchronized ClientConfig getClientConfiguration() {
        if (clientConfiguration == null) {
            clientConfiguration = new RepositoryBasedClientConfig(
                getClientConfigRepository(),
                getAccountRepository(),
                getIdentityRepository(),
                getDropStateRepository(),
                getShareNotificationRepository()
            );
        }
        return clientConfiguration;
    }

    private ClientConfigRepository clientConfigRepository;

    public synchronized ClientConfigRepository getClientConfigRepository() {
        if (clientConfigRepository == null) {
            clientConfigRepository = new SqliteClientConfigRepository(runtimeConfiguration.getConfigDatabase());
        }
        return clientConfigRepository;
    }

    private DropStateRepository dropStateRepository;

    public synchronized DropStateRepository getDropStateRepository() {
        if (dropStateRepository == null) {
            dropStateRepository = new SqliteDropStateRepository();
        }
        return dropStateRepository;
    }

    private ShareNotificationRepository shareNotificationRepository;

    public synchronized ShareNotificationRepository getShareNotificationRepository() {
        if (shareNotificationRepository == null) {
            shareNotificationRepository = new SqliteShareNotificationRepository(
                runtimeConfiguration.getConfigDatabase()
            );
        }
        return shareNotificationRepository;
    }

    private BoxSyncConfigRepository boxSyncConfigRepo;

    @Override
    public synchronized BoxSyncConfigRepository getBoxSyncConfigRepository() {
        if (boxSyncConfigRepo == null) {
            boxSyncConfigRepo = new SqliteBoxSyncConfigRepository();
        }
        return boxSyncConfigRepo;
    }

    private AccountHydrator getAccountHydrator() {
        return new AccountHydrator(
            getEntityManager(),
            getAccountFactory()
        );
    }

    private IdentityHydrator getIdentityHydrator() {
        return new IdentityHydrator(
            getIdentityFactory(),
            getEntityManager(),
            getIdentityDropUrlRepository(),
            getPrefixRepository()
        );
    }

    private ContactHydrator getContactHydrator() {
        return new ContactHydrator(
            getEntityManager(),
            getContactFactory(),
            getContactDropUrlRepository()
        );
    }

    private SqlitePrefixRepository prefixRepository;
    private synchronized SqlitePrefixRepository getPrefixRepository() {
        if (prefixRepository == null) {
            prefixRepository = new SqlitePrefixRepository(runtimeConfiguration.getConfigDatabase());
        }
        return prefixRepository;
    }

    private SqliteIdentityDropUrlRepository dropUrlRepository;
    private synchronized SqliteIdentityDropUrlRepository getIdentityDropUrlRepository() {
        if (dropUrlRepository == null) {
            dropUrlRepository = new SqliteIdentityDropUrlRepository(
                runtimeConfiguration.getConfigDatabase(),
                new DropURLHydrator()
            );
        }
        return dropUrlRepository;
    }

    private SqliteContactDropUrlRepository contactDropUrlRepository;
    private synchronized SqliteContactDropUrlRepository getContactDropUrlRepository() {
        if (contactDropUrlRepository == null) {
            contactDropUrlRepository = new SqliteContactDropUrlRepository(
                runtimeConfiguration.getConfigDatabase(),
                new DropURLHydrator()
            );
        }
        return contactDropUrlRepository;
    }

    private IdentityFactory identityFactory = new DefaultIdentityFactory();
    public IdentityFactory getIdentityFactory() {
        return identityFactory;
    }

    private ContactFactory contactFactory = new DefaultContactFactory();
    public ContactFactory getContactFactory() {
        return contactFactory;
    }

    private AccountFactory accountFactory = new DefaultAccountFactory();
    public AccountFactory getAccountFactory() {
        return accountFactory;
    }

    private EntityManager entityManager = new EntityManager();
    public EntityManager getEntityManager() {
        return entityManager;
    }
}
