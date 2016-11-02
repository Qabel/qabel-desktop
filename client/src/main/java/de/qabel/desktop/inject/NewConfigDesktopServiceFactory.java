package de.qabel.desktop.inject;

import com.google.common.io.Files;
import de.qabel.box.storage.FileMetadataFactory;
import de.qabel.box.storage.jdbc.JdbcFileMetadataFactory;
import de.qabel.chat.repository.sqlite.SqliteChatDropMessageRepository;
import de.qabel.chat.repository.sqlite.SqliteChatShareRepository;
import de.qabel.chat.service.MainChatService;
import de.qabel.chat.service.MainSharingService;
import de.qabel.chat.service.SharingService;
import de.qabel.core.config.factory.*;
import de.qabel.core.crypto.CryptoUtils;
import de.qabel.core.event.EventDispatcher;
import de.qabel.core.event.SubjectEventDispatcher;
import de.qabel.core.http.MainDropConnector;
import de.qabel.core.http.MainDropServer;
import de.qabel.core.index.IndexService;
import de.qabel.core.index.MainIndexService;
import de.qabel.core.repository.*;
import de.qabel.core.repository.sqlite.*;
import de.qabel.core.repository.sqlite.hydrator.AccountHydrator;
import de.qabel.core.repository.sqlite.hydrator.DropURLHydrator;
import de.qabel.core.repository.sqlite.hydrator.IdentityHydrator;
import de.qabel.desktop.config.BoxSyncConfig;
import de.qabel.desktop.config.ClientConfig;
import de.qabel.desktop.config.RepositoryBasedClientConfig;
import de.qabel.desktop.config.factory.BoxSyncConfigFactory;
import de.qabel.desktop.config.factory.DefaultBoxSyncConfigFactory;
import de.qabel.desktop.daemon.drop.ShareNotificationMessage;
import de.qabel.desktop.daemon.sync.worker.index.SyncIndexFactory;
import de.qabel.desktop.daemon.sync.worker.index.sqlite.SqliteSyncIndexFactory;
import de.qabel.desktop.inject.config.RuntimeConfiguration;
import de.qabel.desktop.repository.BoxSyncRepository;
import de.qabel.desktop.repository.DropMessageRepository;
import de.qabel.desktop.repository.ShareNotificationRepository;
import de.qabel.desktop.repository.sqlite.SqliteBoxSyncRepository;
import de.qabel.desktop.repository.sqlite.SqliteClientConfigRepository;
import de.qabel.desktop.repository.sqlite.SqliteDropMessageRepository;
import de.qabel.desktop.repository.sqlite.SqliteShareNotificationRepository;
import de.qabel.desktop.repository.sqlite.hydrator.BoxSyncConfigHydrator;
import de.qabel.desktop.repository.sqlite.hydrator.ShareNotificationMessageHydrator;
import de.qabel.desktop.ui.util.DefaultFileChooserFactory;
import de.qabel.desktop.ui.util.FileChooserFactory;

import java.net.URI;

public class NewConfigDesktopServiceFactory extends RuntimeDesktopServiceFactory {
    public NewConfigDesktopServiceFactory(
        RuntimeConfiguration runtimeConfiguration,
        TransactionManager transactionManager
    )
    {
        super(runtimeConfiguration);
        this.transactionManager = transactionManager;
    }
    protected SqliteIdentityRepository identityRepository;
    @Override
    public synchronized SqliteIdentityRepository getIdentityRepository() {
        if (identityRepository == null) {
            identityRepository = new SqliteIdentityRepository(
                runtimeConfiguration.getConfigDatabase(),
                entityManager,
                getPrefixRepository(),
                getDropUrlRepository()
            );
        }
        return identityRepository;
    }

    protected SqliteContactRepository contactRepository;
    @Override
    public synchronized SqliteContactRepository getContactRepository() {
        if (contactRepository == null) {
            contactRepository = new SqliteContactRepository(
                runtimeConfiguration.getConfigDatabase(),
                getEntityManager(),
                getDropUrlRepository(),
                getIdentityRepository()
            );
        }
        return contactRepository;
    }


    private SqliteAccountRepository accountRepository;
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

    private SqliteChatDropMessageRepository chatDropMessageRepository;
    @Override
    public synchronized SqliteChatDropMessageRepository getChatDropMessageRepository() {
        if (chatDropMessageRepository == null) {
            chatDropMessageRepository = new SqliteChatDropMessageRepository(
                runtimeConfiguration.getConfigDatabase(),
                getEntityManager());
        }
        return chatDropMessageRepository;
    }

    private SqliteChatShareRepository chatShareRepository;
    private SqliteChatShareRepository getChatShareRepository() {
        if (chatShareRepository == null) {
            chatShareRepository = new SqliteChatShareRepository(
                runtimeConfiguration.getConfigDatabase(), getEntityManager());
        }
        return chatShareRepository;
    }

    private MainChatService chatService;

    @Override
    public IndexService getIndexService() {
        return new MainIndexService(
            getIndexServer(),
            getContactRepository(),
            getIdentityRepository()
        );
    }

    @Override
    public synchronized MainChatService getChatService() {
        if (chatService == null) {
            de.qabel.core.http.DropConnector dropConnector =
                new MainDropConnector(new MainDropServer());
            chatService = new MainChatService(
                dropConnector,
                getIdentityRepository(),
                getContactRepository(),
                getChatDropMessageRepository(),
                getDropStateRepository(),
                getChatSharingService()
            );
        }
        return chatService;
    }

    private FileMetadataFactory fileMetadataFactory;
    private FileMetadataFactory getFileMetadataFactory() {
        if (fileMetadataFactory == null) {
            fileMetadataFactory = new JdbcFileMetadataFactory(
                Files.createTempDir(),
                PragmaVersionAdapter::new
            );
        }
        return fileMetadataFactory;
    }

    private SharingService sharingService;
    private SharingService getChatSharingService() {
        if (sharingService == null) {
            sharingService = new MainSharingService(
                getChatShareRepository(),
                getContactRepository(),
                Files.createTempDir(),
                getFileMetadataFactory(),
                new CryptoUtils());
        }
        return sharingService;

    }

    @Override
    public URI getAccountingUri() {
        return runtimeConfiguration.getAccountingUri();
    }

    @Override
    public URI getBlockUri() {
        return runtimeConfiguration.getBlockUri();
    }

    private ClientConfig clientConfiguration;

    @Override
    public synchronized ClientConfig getClientConfiguration() {
        if (clientConfiguration == null) {
            clientConfiguration = new RepositoryBasedClientConfig(
                getClientConfigRepository(),
                getAccountRepository(),
                getIdentityRepository(),
                getDropStateRepository(),
                getShareNotificationRepository(),
                getEventDispatcher()
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
            dropStateRepository = new SqliteDropStateRepository(runtimeConfiguration.getConfigDatabase(), getEntityManager());
        }
        return dropStateRepository;
    }

    private ShareNotificationRepository shareNotificationRepository;

    @Override
    public synchronized ShareNotificationRepository getShareNotificationRepository() {
        if (shareNotificationRepository == null) {
            shareNotificationRepository = new SqliteShareNotificationRepository(
                runtimeConfiguration.getConfigDatabase(),
                getShareNotificationMessageHydrator()
            );
        }
        return shareNotificationRepository;
    }

    public Hydrator<ShareNotificationMessage> getShareNotificationMessageHydrator() {
        return new ShareNotificationMessageHydrator(getEntityManager());
    }

    private BoxSyncRepository boxSyncConfigRepo;

    @Override
    public synchronized BoxSyncRepository getBoxSyncConfigRepository() {
        if (boxSyncConfigRepo == null) {
            boxSyncConfigRepo = new SqliteBoxSyncRepository(
                runtimeConfiguration.getConfigDatabase(),
                getBoxSyncConfigHydrator()
            );
        }
        return boxSyncConfigRepo;
    }

    private SqliteDropMessageRepository dropMessageRepository;
    @Override
    public synchronized DropMessageRepository getDropMessageRepository() {
        if (dropMessageRepository == null) {
            dropMessageRepository = new SqliteDropMessageRepository(
                runtimeConfiguration.getConfigDatabase(),
                entityManager,
                getIdentityRepository(),
                getContactRepository()
            );
        }
        return dropMessageRepository;
    }

    private Hydrator<BoxSyncConfig> getBoxSyncConfigHydrator() {
        return new BoxSyncConfigHydrator(
            getEntityManager(),
            getBoxSyncConfigFactory(),
            getIdentityRepository(),
            getAccountRepository()
        );
    }

    @Override
    public BoxSyncConfigFactory getBoxSyncConfigFactory() {
        return new DefaultBoxSyncConfigFactory(getSyncIndexFactory());
    }

    private EventDispatcher eventDispatcher = new SubjectEventDispatcher();
    @Override
    public EventDispatcher getEventDispatcher() {
        return eventDispatcher;
    }

    private SyncIndexFactory getSyncIndexFactory() {
        return new SqliteSyncIndexFactory();
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
            getDropUrlRepository(),
            getPrefixRepository()
        );
    }

    private SqlitePrefixRepository prefixRepository;
    private synchronized SqlitePrefixRepository getPrefixRepository() {
        if (prefixRepository == null) {
            prefixRepository = new SqlitePrefixRepository(
                runtimeConfiguration.getConfigDatabase(),
                getEntityManager()
            );
        }
        return prefixRepository;
    }

    private SqliteDropUrlRepository dropUrlRepository;
    private synchronized SqliteDropUrlRepository getDropUrlRepository() {
        if (dropUrlRepository == null) {
            dropUrlRepository = new SqliteDropUrlRepository(
                runtimeConfiguration.getConfigDatabase(),
                new DropURLHydrator()
            );
        }
        return dropUrlRepository;
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

    private TransactionManager transactionManager;

    @Override
    public TransactionManager getTransactionManager() {
        return transactionManager;
    }

    @Override
    public FileChooserFactory getFileChooserFactory() {
        return new DefaultFileChooserFactory();
    }
}
