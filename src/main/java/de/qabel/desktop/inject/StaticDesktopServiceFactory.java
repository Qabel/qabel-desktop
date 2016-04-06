package de.qabel.desktop.inject;

import de.qabel.core.accounting.AccountingHTTP;
import de.qabel.core.accounting.AccountingProfile;
import de.qabel.core.config.Account;
import de.qabel.core.config.AccountingServer;
import de.qabel.core.config.Persistence;
import de.qabel.core.config.SQLitePersistence;
import de.qabel.core.http.DropHTTP;
import de.qabel.desktop.BlockSharingService;
import de.qabel.desktop.MagicEvilBlockUriProvider;
import de.qabel.desktop.ServiceFactory;
import de.qabel.desktop.SharingService;
import de.qabel.desktop.config.ClientConfiguration;
import de.qabel.desktop.config.factory.*;
import de.qabel.desktop.crashReports.CrashReportHandler;
import de.qabel.desktop.crashReports.HockeyApp;
import de.qabel.desktop.daemon.NetworkStatus;
import de.qabel.desktop.daemon.management.DefaultTransferManager;
import de.qabel.desktop.daemon.management.MonitoredTransferManager;
import de.qabel.desktop.daemon.management.TransferManager;
import de.qabel.desktop.inject.config.RuntimeConfiguration;
import de.qabel.desktop.repository.*;
import de.qabel.desktop.repository.persistence.*;
import de.qabel.desktop.ui.actionlog.item.renderer.MessageRendererFactory;
import de.qabel.desktop.ui.actionlog.item.renderer.PlaintextMessageRenderer;
import de.qabel.desktop.ui.connector.DropConnector;
import de.qabel.desktop.ui.connector.HttpDropConnector;
import javafx.stage.Stage;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class StaticDesktopServiceFactory extends DefaultServiceFactory implements DesktopServices {
    private static Map<String, Method> creators = new HashMap<>();

    static {
        for (Method method : StaticDesktopServiceFactory.class.getDeclaredMethods()) {
            method.setAccessible(true);
            if (method.isAnnotationPresent(Create.class)) {
                String createdInstance = method.getAnnotation(Create.class).name();
                creators.put(createdInstance, method);
            } else if (method.isAnnotationPresent(Creates.class)) {
                for (Create create : method.getAnnotation(Creates.class).value()) {
                    creators.put(create.name(), method);
                }
            }
        }
    }

    private RuntimeConfiguration runtimeConfiguration;

    public StaticDesktopServiceFactory(RuntimeConfiguration runtimeConfiguration) {
        this.runtimeConfiguration = runtimeConfiguration;
    }

    @Override
    public Object getByType(Class type) {
        Object instance = super.getByType(type);
        if (instance != null) {
            return instance;
        }
        for (Method method : creators.values()) {
            if (type.isAssignableFrom(method.getReturnType())) {
                return invoke(method);
            }
        }
        return null;
    }

    @Override
    public synchronized Object get(String key) {
        if (!cache.containsKey(key)) {
            cache.put(key, generate(key));
        }
        return super.get(key);
    }

    private Object generate(String key) {
        if (!creators.containsKey(key)) {
            throw new IllegalArgumentException("failed to create instance for " + key);
        }

        Method method = creators.get(key);
        return invoke(method);
    }

    private Object invoke(Method method) {
        try {
            return method.invoke(this);
        } catch (IllegalAccessException | InvocationTargetException e) {
            throw new IllegalStateException("failed to call public method on correct class: " + e.getMessage(), e);
        }
    }

    private TransferManager transferManager;

    @Override
    @Create(name = "loadManager")
    @Create(name = "transferManager")
    public synchronized TransferManager getTransferManager() {
        if (transferManager == null) {
            transferManager = new MonitoredTransferManager(new DefaultTransferManager());
        }
        return transferManager;
    }

    private IdentityRepository identityRepository;

    @Override
    @Create(name = "identityRepository")
    public synchronized IdentityRepository getIdentityRepository() {
        if (identityRepository == null) {
            identityRepository = new PersistenceIdentityRepository(getPersistence());
        }
        return identityRepository;
    }

    private IdentityBuilderFactory identityBuilderFactory;

    @Override
    @Create(name = "identityBuilderFactory")
    public synchronized IdentityBuilderFactory getIdentityBuilderFactory() {
        if (identityBuilderFactory == null) {
            identityBuilderFactory = new IdentityBuilderFactory(getDropUrlGenerator());
        }
        return identityBuilderFactory;
    }

    AccountRepository accountRepository;

    @Override
    @Create(name = "accountingRepository")
    public synchronized AccountRepository getAccountRepository() {
        if (accountRepository == null) {
            accountRepository = new PersistenceAccountRepository(getPersistence());
        }
        return accountRepository;
    }

    private DropUrlGenerator dropUrlGenerator;

    @Override
    @Create(name = "dropUrlGenerator")
    public synchronized DropUrlGenerator getDropUrlGenerator() {
        if (dropUrlGenerator == null) {
            dropUrlGenerator = new DropUrlGenerator(runtimeConfiguration.getDropUri());
        }
        return dropUrlGenerator;
    }

    private Persistence<String> persistence;

    @Deprecated
    @Create(name = "persistence")
    private synchronized Persistence<String> getPersistence() {
        if (persistence == null) {
            persistence = new SQLitePersistence(runtimeConfiguration.getPersistenceDatabaseFile().toFile().getAbsolutePath());
        }
        return persistence;
    }

    private ContactRepository contactRepository;

    @Override
    @Create(name = "contactRepository")
    public synchronized ContactRepository getContactRepository() {
        if (contactRepository == null) {
            contactRepository = new PersistenceContactRepository(getPersistence());
        }
        return contactRepository;
    }

    private DropMessageRepository dropMessageRepository;

    @Override
    @Create(name = "dropMessageRepository")
    public synchronized DropMessageRepository getDropMessageRepository() {
        if (dropMessageRepository == null) {
            dropMessageRepository = new PersistenceDropMessageRepository(getPersistence());
        }
        return dropMessageRepository;
    }

    private ClientConfiguration clientConfiguration;

    @Override
    @Create(name = "clientConfiguration")
    @Create(name = "config")
    public synchronized ClientConfiguration getClientConfiguration() {
        if (clientConfiguration == null) {
            ClientConfigurationRepository repo = getClientConfigurationRepository();
            clientConfiguration = repo.load();
            clientConfiguration.addObserver((o, arg) -> repo.save(clientConfiguration));

            if (!clientConfiguration.hasDeviceId()) {
                clientConfiguration.setDeviceId(generateNewDeviceId());
            }
        }
        return clientConfiguration;
    }

    private String generateNewDeviceId() {
        return UUID.randomUUID().toString();
    }

    private ClientConfigurationRepository clientConfigurationRepository;

    @Override
    public synchronized ClientConfigurationRepository getClientConfigurationRepository() {
        if (clientConfigurationRepository == null) {
            clientConfigurationRepository = new PersistenceClientConfigurationRepository(
                getPersistence(),
                new ClientConfigurationFactory(),
                getIdentityRepository(),
                getAccountRepository()
            );
        }
        return clientConfigurationRepository;
    }

    private NetworkStatus networkStatus = new NetworkStatus();

    @Override
    @Create(name = "networkStatus")
    public NetworkStatus getNetworkStatus() {
        return networkStatus;
    }

    private DropConnector dropConnector;

    @Override
    @Create(name = "dropConnector")
    public synchronized DropConnector getDropConnector() {
        if (dropConnector == null) {
            dropConnector = new HttpDropConnector(getNetworkStatus(), new DropHTTP());
        }
        return dropConnector;
    }

    @Override
    @Create(name = "reportHandler")
    public synchronized CrashReportHandler getCrashReportHandler() {
        return new HockeyApp();
    }

    private MessageRendererFactory messageRendererFactory;

    @Override
    @Create(name = "messageRendererFactory")
    public synchronized MessageRendererFactory getDropMessageRendererFactory() {
        if (messageRendererFactory == null) {
            messageRendererFactory = new MessageRendererFactory();
            messageRendererFactory.setFallbackRenderer(new PlaintextMessageRenderer());
        }
        return messageRendererFactory;
    }

    private SharingService sharingService;

    @Override
    @Create(name = "sharingService")
    public synchronized SharingService getSharingService() {
        if (sharingService == null) {
            sharingService = new BlockSharingService(getDropMessageRepository(), getDropConnector());
        }
        return sharingService;
    }

    private BoxVolumeFactory boxVolumeFactory;

    private AccountingHTTP accountingHTTP;

    @Override
    @Create(name = "boxVolumeFactory")
    public synchronized BoxVolumeFactory getBoxVolumeFactory() throws IOException {
        if (boxVolumeFactory == null) {
            boxVolumeFactory = new BlockBoxVolumeFactory(
                getClientConfiguration().getDeviceId().getBytes(),
                getAccountingClient(),
                getIdentityRepository()
            );
        }
        return boxVolumeFactory;
    }

    @Override
    @Create(name = "accountingClient")
    public synchronized AccountingHTTP getAccountingClient() {
        if (accountingHTTP == null) {
            Account acc = getClientConfiguration().getAccount();
            if (acc == null) {
                throw new IllegalStateException("cannot get accounting client without valid account");
            }
            try {
                AccountingServer server = new AccountingServer(
                    new URI(acc.getProvider()),
                    new URI(MagicEvilBlockUriProvider.getBlockUri(acc)),
                    acc.getUser(),
                    acc.getAuth()
                );
                accountingHTTP = new AccountingHTTP(server, new AccountingProfile());
            } catch (URISyntaxException e) {
                throw new IllegalStateException("cannot get accounting client without valid account: " + e.getMessage(), e);
            }
        }
        return accountingHTTP;
    }

    @Create(name = "primaryStage")
    public Stage getPrimaryStage() {
        return runtimeConfiguration.getPrimaryStage();
    }

}
