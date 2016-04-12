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
import de.qabel.desktop.SharingService;
import de.qabel.desktop.config.ClientConfig;
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
import java.net.URI;
import java.net.URISyntaxException;
import java.util.UUID;

public abstract class StaticDesktopServiceFactory extends AnnotatedDesktopServiceFactory implements DesktopServices {
    protected RuntimeConfiguration runtimeConfiguration;

    public StaticDesktopServiceFactory(RuntimeConfiguration runtimeConfiguration) {
        this.runtimeConfiguration = runtimeConfiguration;
    }

    private TransferManager transferManager;

    @Override
    public synchronized TransferManager getTransferManager() {
        if (transferManager == null) {
            transferManager = new MonitoredTransferManager(new DefaultTransferManager());
        }
        return transferManager;
    }

    protected IdentityRepository identityRepository;

    @Override
    public synchronized IdentityRepository getIdentityRepository() {
        if (identityRepository == null) {
            identityRepository = new PersistenceIdentityRepository(getPersistence());
        }
        return identityRepository;
    }

    private IdentityBuilderFactory identityBuilderFactory;

    @Override
    public synchronized IdentityBuilderFactory getIdentityBuilderFactory() {
        if (identityBuilderFactory == null) {
            identityBuilderFactory = new IdentityBuilderFactory(getDropUrlGenerator());
        }
        return identityBuilderFactory;
    }

    AccountRepository accountRepository;

    @Override
    public synchronized AccountRepository getAccountRepository() {
        if (accountRepository == null) {
            accountRepository = new PersistenceAccountRepository(getPersistence());
        }
        return accountRepository;
    }

    private DropUrlGenerator dropUrlGenerator;

    @Override
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

    protected ContactRepository contactRepository;

    @Override
    public synchronized ContactRepository getContactRepository() {
        if (contactRepository == null) {
            contactRepository = new PersistenceContactRepository(getPersistence());
        }
        return contactRepository;
    }

    private DropMessageRepository dropMessageRepository;

    @Override
    public synchronized DropMessageRepository getDropMessageRepository() {
        if (dropMessageRepository == null) {
            dropMessageRepository = new PersistenceDropMessageRepository(getPersistence());
        }
        return dropMessageRepository;
    }

    protected ClientConfig clientConfiguration;

    @Override
    public synchronized ClientConfig getClientConfiguration() {
        if (clientConfiguration == null) {
            ClientConfigurationRepository repo = getClientConfigurationRepository();
            clientConfiguration = repo.load();

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
    public NetworkStatus getNetworkStatus() {
        return networkStatus;
    }

    private DropConnector dropConnector;

    @Override
    public synchronized DropConnector getDropConnector() {
        if (dropConnector == null) {
            dropConnector = new HttpDropConnector(getNetworkStatus(), new DropHTTP());
        }
        return dropConnector;
    }

    @Override
    public synchronized CrashReportHandler getCrashReportHandler() {
        return new HockeyApp();
    }

    private MessageRendererFactory messageRendererFactory;

    @Override
    public synchronized MessageRendererFactory getDropMessageRendererFactory() {
        if (messageRendererFactory == null) {
            messageRendererFactory = new MessageRendererFactory();
            messageRendererFactory.setFallbackRenderer(new PlaintextMessageRenderer());
        }
        return messageRendererFactory;
    }

    private SharingService sharingService;

    @Override
    public synchronized SharingService getSharingService() {
        if (sharingService == null) {
            sharingService = new BlockSharingService(getDropMessageRepository(), getDropConnector());
        }
        return sharingService;
    }

    private BoxVolumeFactory boxVolumeFactory;

    private AccountingHTTP accountingHTTP;

    @Override
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

    @Override
    public Stage getPrimaryStage() {
        return runtimeConfiguration.getPrimaryStage();
    }

    public abstract BoxSyncRepository getBoxSyncConfigRepository();

    public abstract ShareNotificationRepository getShareNotificationRepository();
}
