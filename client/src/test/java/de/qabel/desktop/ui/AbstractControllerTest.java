package de.qabel.desktop.ui;

import com.airhacks.afterburner.injection.Injector;
import com.airhacks.afterburner.views.QabelFXMLView;
import de.qabel.box.storage.factory.BoxVolumeFactory;
import de.qabel.chat.repository.ChatDropMessageRepository;
import de.qabel.chat.repository.inmemory.InMemoryChatDropMessageRepository;
import de.qabel.core.accounting.BoxClientStub;
import de.qabel.core.config.Account;
import de.qabel.core.config.Identity;
import de.qabel.core.config.factory.DropUrlGenerator;
import de.qabel.core.config.factory.IdentityBuilderFactory;
import de.qabel.core.event.EventDispatcher;
import de.qabel.core.event.SubjectEventDispatcher;
import de.qabel.core.index.IndexService;
import de.qabel.core.repository.*;
import de.qabel.core.repository.inmemory.*;
import de.qabel.desktop.BlockSharingService;
import de.qabel.desktop.SharingService;
import de.qabel.desktop.config.ClientConfig;
import de.qabel.desktop.config.FilesAbout;
import de.qabel.desktop.config.RepositoryBasedClientConfig;
import de.qabel.desktop.config.factory.DefaultBoxSyncConfigFactory;
import de.qabel.desktop.crashReports.StubCrashReportHandler;
import de.qabel.desktop.daemon.NetworkStatus;
import de.qabel.desktop.daemon.management.DefaultTransferManager;
import de.qabel.desktop.daemon.sync.SyncDaemon;
import de.qabel.desktop.daemon.sync.worker.FakeSyncerFactory;
import de.qabel.desktop.daemon.sync.worker.index.SyncIndexFactory;
import de.qabel.desktop.daemon.sync.worker.index.sqlite.SqliteSyncIndexFactory;
import de.qabel.desktop.inject.CompositeServiceFactory;
import de.qabel.desktop.repository.BoxSyncRepository;
import de.qabel.desktop.repository.DropMessageRepository;
import de.qabel.desktop.repository.ShareNotificationRepository;
import de.qabel.desktop.repository.inmemory.*;
import de.qabel.desktop.ui.actionlog.item.renderer.FXMessageRendererFactory;
import de.qabel.desktop.ui.actionlog.item.renderer.PlaintextMessageRenderer;
import de.qabel.desktop.ui.connector.DropConnector;
import de.qabel.desktop.ui.inject.AfterburnerInjector;
import de.qabel.desktop.ui.inject.RecursiveInjectionInstanceSupplier;
import de.qabel.desktop.ui.tray.DropMessageNotificator;
import de.qabel.desktop.ui.util.CallbackFileChooserFactory;
import de.qabel.desktop.util.Translator;
import javafx.beans.property.SimpleListProperty;
import javafx.scene.Parent;
import javafx.scene.layout.Pane;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.simple.SimpleLogger;
import org.apache.logging.log4j.util.PropertiesUtil;
import org.apache.logging.slf4j.Log4jLogger;
import org.junit.After;
import org.junit.Before;
import org.slf4j.Logger;
import rx.schedulers.JavaFxScheduler;
import rx.schedulers.Schedulers;

import java.net.URI;
import java.util.Locale;
import java.util.Properties;
import java.util.function.Function;

import static org.mockito.Mockito.mock;

public class AbstractControllerTest extends AbstractFxTest {
    protected static Logger logger;
    protected TransactionManager transactionManager = new InMemoryTransactionManager();
    protected CompositeServiceFactory diContainer = new CompositeServiceFactory();
    protected IdentityRepository identityRepository = new InMemoryIdentityRepository();
    protected ClientConfig clientConfiguration;
    protected IdentityBuilderFactory identityBuilderFactory;
    protected ContactRepository contactRepository = new InMemoryContactRepository();
    protected DefaultTransferManager transferManager;
    protected BoxVolumeFactory boxVolumeFactory;
    protected DropMessageRepository dropMessageRepository = new InMemoryDropMessageRepository();
    protected DropConnector httpDropConnector = new InMemoryHttpDropConnector();
    protected StubCrashReportHandler crashReportHandler = new StubCrashReportHandler();
    protected SharingService sharingService = new BlockSharingService(dropMessageRepository, httpDropConnector);
    protected NetworkStatus networkStatus = new NetworkStatus();
    protected Identity identity;
    protected ClientConfigRepository clientConfigRepository = new InMemoryClientConfigRepository();
    protected AccountRepository accountRepository = new InMemoryAccountRepository();
    protected ChatDropMessageRepository chatDropMessageRepository = new InMemoryChatDropMessageRepository();
    protected DropStateRepository dropStateRepository = new InMemoryDropStateRepository();
    protected ShareNotificationRepository shareNotificationRepository = new InMemoryShareNotificationRepository();
    protected BoxSyncRepository boxSyncRepository = new InMemoryBoxSyncRepository();
    protected IndexService indexService = mock(IndexService.class);
    protected SyncDaemon syncDaemon;
    protected Account account;
    protected BoxClientStub boxClient = new BoxClientStub();
    protected Parent layoutWindow = new Pane();
    protected int remoteDebounceTimeout;
    protected EventDispatcher eventDispatcher = new SubjectEventDispatcher();
    protected DropMessageNotificator dropMessageNotificator;
    protected Translator translator;

    protected FXMessageRendererFactory fxMessageRendererFactory = new FXMessageRendererFactory();
    protected PlaintextMessageRenderer plaintextMessageRenderer = new PlaintextMessageRenderer();

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
        Locale.setDefault(new Locale("te", "ST"));
        translator = new Translator(QabelFXMLView.getDefaultResourceBundle());
        diContainer.put("translator", translator);
        clientConfiguration = new RepositoryBasedClientConfig(
            clientConfigRepository,
            accountRepository,
            identityRepository,
            dropStateRepository,
            shareNotificationRepository,
            eventDispatcher
        );
        diContainer.put("resourceBundle", QabelFXMLView.getDefaultResourceBundle());
        diContainer.put("fileChooserFactory", new CallbackFileChooserFactory(() -> null));
        diContainer.put("eventDispatcher", eventDispatcher);
        diContainer.put("eventSource", eventDispatcher);
        diContainer.put("eventSink", eventDispatcher);
        diContainer.put("indexService", indexService);
        diContainer.put("remoteDebounceTimeout", remoteDebounceTimeout);
        diContainer.put("clientConfiguration", clientConfiguration);
        diContainer.put("layoutWindow", layoutWindow);
        diContainer.put("dropUrlGenerator", new DropUrlGenerator("http://localhost:5000"));
        identityBuilderFactory = new IdentityBuilderFactory((DropUrlGenerator) diContainer.get("dropUrlGenerator"));
        diContainer.put("identityBuilderFactory", identityBuilderFactory);
        account = new Account("a", "b", "c");
        diContainer.put("account", account);
        clientConfiguration.setAccount(account);
        diContainer.put("identityRepository", identityRepository);
        diContainer.put("contactRepository", contactRepository);
        boxVolumeFactory = mock(BoxVolumeFactory.class);
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

        fxMessageRendererFactory.setFallbackRenderer(plaintextMessageRenderer);
        diContainer.put("messageRendererFactory", fxMessageRendererFactory);

        SyncIndexFactory syncIndexFactory = new SqliteSyncIndexFactory();
        diContainer.put("boxSyncConfigFactory", new DefaultBoxSyncConfigFactory(syncIndexFactory));
        diContainer.put("boxSyncIndexFactory", syncIndexFactory);
        diContainer.put("aboutFilesContent", new FilesAbout());

        diContainer.put("boxClient", boxClient);

        syncDaemon = new SyncDaemon(new SimpleListProperty<>(), new FakeSyncerFactory());
        diContainer.put("syncDaemon", syncDaemon);
        diContainer.put("accountingUri", new URI("http://localhost:9696"));

        diContainer.put("fxScheduler", JavaFxScheduler.getInstance());
        diContainer.put("ioScheduler", Schedulers.immediate());
        diContainer.put("computationScheduler", Schedulers.immediate());

        AfterburnerInjector.setConfigurationSource(key -> diContainer.get((String) key));
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
