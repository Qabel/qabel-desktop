package de.qabel.desktop.ui;

import com.airhacks.afterburner.injection.Injector;
import com.airhacks.afterburner.views.QabelFXMLView;
import de.qabel.core.config.Account;
import de.qabel.core.config.Identity;
import de.qabel.desktop.BlockSharingService;
import de.qabel.desktop.ServiceFactory;
import de.qabel.desktop.SharingService;
import de.qabel.desktop.config.ClientConfig;
import de.qabel.desktop.config.RepositoryBasedClientConfig;
import de.qabel.desktop.config.factory.DropUrlGenerator;
import de.qabel.desktop.config.factory.IdentityBuilderFactory;
import de.qabel.desktop.crashReports.StubCrashReportHandler;
import de.qabel.desktop.daemon.NetworkStatus;
import de.qabel.desktop.daemon.management.BoxVolumeFactoryStub;
import de.qabel.desktop.daemon.management.DefaultTransferManager;
import de.qabel.desktop.repository.*;
import de.qabel.desktop.repository.Stub.InMemoryContactRepository;
import de.qabel.desktop.repository.Stub.StubDropMessageRepository;
import de.qabel.desktop.repository.inmemory.*;
import de.qabel.desktop.inject.DefaultServiceFactory;
import de.qabel.desktop.ui.actionlog.item.renderer.FXMessageRendererFactory;
import de.qabel.desktop.ui.actionlog.item.renderer.PlaintextMessageRenderer;
import de.qabel.desktop.ui.connector.DropConnector;
import de.qabel.desktop.ui.inject.RecursiveInjectionInstanceSupplier;
import org.junit.After;
import org.junit.Before;

import java.util.function.Function;

public class AbstractControllerTest extends AbstractFxTest {
    protected TransactionManager transactionManager = new InMemoryTransactionManager();
    protected ServiceFactory diContainer = new DefaultServiceFactory();
    protected IdentityRepository identityRepository = new InMemoryIdentityRepository();
    protected ClientConfig clientConfiguration;
    protected IdentityBuilderFactory identityBuilderFactory;
    protected ContactRepository contactRepository = new InMemoryContactRepository();
    protected DefaultTransferManager loadManager;
    protected BoxVolumeFactoryStub boxVolumeFactory;
    protected DropMessageRepository dropMessageRepository = new StubDropMessageRepository();
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
        Account account = new Account("a", "b", "c");
        diContainer.put("account", account);
        clientConfiguration.setAccount(account);
        diContainer.put("identityRepository", identityRepository);
        diContainer.put("contactRepository", contactRepository);
        boxVolumeFactory = new BoxVolumeFactoryStub();
        diContainer.put("boxVolumeFactory", boxVolumeFactory);
        loadManager = new DefaultTransferManager();
        diContainer.put("loadManager", loadManager);
        diContainer.put("dropMessageRepository", dropMessageRepository);
        diContainer.put("dropConnector", httpDropConnector);
        diContainer.put("transferManager", new DefaultTransferManager());
        diContainer.put("sharingService", sharingService);
        diContainer.put("reportHandler", crashReportHandler);
        diContainer.put("networkStatus", networkStatus);
        diContainer.put("boxSyncConfigRepository", boxSyncRepository);
        diContainer.put("boxSyncRepository", boxSyncRepository);
        diContainer.put("transactionManager", transactionManager);
        FXMessageRendererFactory FXMessageRendererFactory = new FXMessageRendererFactory();
        FXMessageRendererFactory.setFallbackRenderer(new PlaintextMessageRenderer());
        diContainer.put("messageRendererFactory", FXMessageRendererFactory);
        Injector.setConfigurationSource(key -> diContainer.get((String)key));
        Injector.setInstanceSupplier(new RecursiveInjectionInstanceSupplier(diContainer));

        identity = identityBuilderFactory.factory().withAlias("TestAlias").build();
        clientConfiguration.selectIdentity(identity);
        QabelFXMLView.unloadDefaultResourceBundle();
    }

    @After
    public void tearDown() throws Exception {
        try {
            Injector.forgetAll();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    protected Function<String, Object> generateInjection(String name, Object instance) {
        return requestedName -> requestedName.equals(name) ? instance : null;
    }
}
