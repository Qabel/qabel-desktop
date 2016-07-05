package de.qabel.desktop.ui;

import com.airhacks.afterburner.injection.Injector;
import com.airhacks.afterburner.views.QabelFXMLView;
import de.qabel.core.config.Account;
import de.qabel.core.config.Identity;
import de.qabel.core.config.factory.DropUrlGenerator;
import de.qabel.core.config.factory.IdentityBuilderFactory;
import de.qabel.core.repository.*;
import de.qabel.core.repository.inmemory.*;
import de.qabel.desktop.BlockSharingService;
import de.qabel.desktop.ServiceFactory;
import de.qabel.desktop.SharingService;
import de.qabel.desktop.config.ClientConfig;
import de.qabel.desktop.config.FilesAbout;
import de.qabel.desktop.config.RepositoryBasedClientConfig;
import de.qabel.desktop.config.factory.DefaultBoxSyncConfigFactory;
import de.qabel.desktop.crashReports.StubCrashReportHandler;
import de.qabel.desktop.daemon.NetworkStatus;
import de.qabel.desktop.daemon.management.BoxVolumeFactoryStub;
import de.qabel.desktop.daemon.management.DefaultTransferManager;
import de.qabel.desktop.daemon.sync.SyncDaemon;
import de.qabel.desktop.daemon.sync.worker.FakeSyncerFactory;
import de.qabel.desktop.daemon.sync.worker.index.SyncIndexFactory;
import de.qabel.desktop.daemon.sync.worker.index.sqlite.SqliteSyncIndexFactory;
import de.qabel.desktop.inject.DefaultServiceFactory;
import de.qabel.desktop.repository.BoxSyncRepository;
import de.qabel.desktop.repository.DropMessageRepository;
import de.qabel.desktop.repository.ShareNotificationRepository;
import de.qabel.desktop.repository.inmemory.*;
import de.qabel.desktop.ui.actionlog.item.renderer.FXMessageRendererFactory;
import de.qabel.desktop.ui.actionlog.item.renderer.PlaintextMessageRenderer;
import de.qabel.desktop.ui.connector.DropConnector;
import de.qabel.desktop.ui.inject.AfterburnerInjector;
import de.qabel.desktop.ui.inject.RecursiveInjectionInstanceSupplier;
import javafx.beans.property.SimpleListProperty;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.simple.SimpleLogger;
import org.apache.logging.log4j.util.PropertiesUtil;
import org.apache.logging.slf4j.Log4jLogger;
import org.junit.After;
import org.junit.Before;
import org.slf4j.Logger;

import java.net.URI;
import java.util.Properties;
import java.util.function.Function;

public class AbstractControllerTest extends AbstractFxTest {
    protected static Logger logger;
    protected TransactionManager transactionManager = new InMemoryTransactionManager();
    protected ServiceFactory diContainer = new DefaultServiceFactory();
    protected IdentityRepository identityRepository = new InMemoryIdentityRepository();
    protected ClientConfig clientConfiguration;
    protected IdentityBuilderFactory identityBuilderFactory;
    protected ContactRepository contactRepository = new InMemoryContactRepository();
    protected DefaultTransferManager transferManager;
    protected BoxVolumeFactoryStub boxVolumeFactory;
    protected DropMessageRepository dropMessageRepository = new InMemoryDropMessageRepository();
    protected DropConnector httpDropConnector = new InMemoryHttpDropConnector();
    protected StubCrashReportHandler crashReportHandler = new StubCrashReportHandler();
    protected SharingService sharingService = new BlockSharingService(dropMessageRepository, httpDropConnector);
    protected NetworkStatus networkStatus = new NetworkStatus();
    protected Identity identity;
    protected ClientConfigRepository clientConfigRepository = new InMemoryClientConfigRepository();
    protected AccountRepository accountRepository = new InMemoryAccountRepository();
    protected DropStateRepository dropStateRepository = new InMemoryDropStateRepository();
    protected ShareNotificationRepository shareNotificationRepository = new InMemoryShareNotificationRepository();
    protected BoxSyncRepository boxSyncRepository = new InMemoryBoxSyncRepository();
    protected SyncDaemon syncDaemon;
    protected Account account;

    static {
        logger = createLogger();
    }

    public static Logger createLogger() {
        SimpleLogger test = new SimpleLogger(
            "testLogger",
            Level.ALL,
            false,
            true,
            false,
            false,
            "",
            null,
            new PropertiesUtil(new Properties()),
            System.err
        );
        return new Log4jLogger(
            test,
            "testLogger"
        );
    }

    @Before
    public void setUp() throws Exception {
        clientConfiguration = new RepositoryBasedClientConfig(
            clientConfigRepository,
            accountRepository,
            identityRepository,
            dropStateRepository,
            shareNotificationRepository
        );
        diContainer.put("clientConfiguration", clientConfiguration);
        diContainer.put("dropUrlGenerator", new DropUrlGenerator("http://localhost:5000"));
        identityBuilderFactory = new IdentityBuilderFactory((DropUrlGenerator) diContainer.get("dropUrlGenerator"));
        diContainer.put("identityBuilderFactory", identityBuilderFactory);
        account = new Account("a", "b", "c");
        diContainer.put("account", account);
        clientConfiguration.setAccount(account);
        diContainer.put("identityRepository", identityRepository);
        diContainer.put("contactRepository", contactRepository);
        boxVolumeFactory = new BoxVolumeFactoryStub();
        diContainer.put("boxVolumeFactory", boxVolumeFactory);
        transferManager = new DefaultTransferManager();
        diContainer.put("transferManager", transferManager);
        diContainer.put("dropMessageRepository", dropMessageRepository);
        diContainer.put("dropConnector", httpDropConnector);
        diContainer.put("sharingService", sharingService);
        diContainer.put("reportHandler", crashReportHandler);
        diContainer.put("networkStatus", networkStatus);
        diContainer.put("boxSyncConfigRepository", boxSyncRepository);
        diContainer.put("boxSyncRepository", boxSyncRepository);
        diContainer.put("transactionManager", transactionManager);
        FXMessageRendererFactory FXMessageRendererFactory = new FXMessageRendererFactory();
        FXMessageRendererFactory.setFallbackRenderer(new PlaintextMessageRenderer());
        diContainer.put("messageRendererFactory", FXMessageRendererFactory);
        SyncIndexFactory syncIndexFactory = new SqliteSyncIndexFactory();
        diContainer.put("boxSyncConfigFactory", new DefaultBoxSyncConfigFactory(syncIndexFactory));
        diContainer.put("boxSyncIndexFactory", syncIndexFactory);
        diContainer.put("aboutFilesContent", new FilesAbout());

        syncDaemon = new SyncDaemon(new SimpleListProperty<>(), new FakeSyncerFactory());
        diContainer.put("syncDaemon", syncDaemon);
        diContainer.put("accountingUri", new URI("http://localhost:9696"));

        AfterburnerInjector.setConfigurationSource(key -> diContainer.get((String)key));
        AfterburnerInjector.setInstanceSupplier(new RecursiveInjectionInstanceSupplier(diContainer));

        identity = identityBuilderFactory.factory().withAlias("TestAlias").build();
        clientConfiguration.selectIdentity(identity);
        QabelFXMLView.unloadDefaultResourceBundle();
    }

    @After
    public void tearDown() throws Exception {
        try {
            Injector.forgetAll();
            AfterburnerInjector.forgetAll();
        } catch (Exception e) {
            logger.error("failed to tear down injector", e);
        }
    }

    protected Function<String, Object> generateInjection(String name, Object instance) {
        return requestedName -> requestedName.equals(name) ? instance : null;
    }
}
