package de.qabel.desktop.inject;

import de.qabel.desktop.util.UTF8Converter;
import de.qabel.core.accounting.AccountingHTTP;
import de.qabel.core.accounting.AccountingProfile;
import de.qabel.core.config.Account;
import de.qabel.core.config.AccountingServer;
import de.qabel.core.http.DropHTTP;
import de.qabel.desktop.BlockSharingService;
import de.qabel.desktop.MagicEvilBlockUriProvider;
import de.qabel.desktop.SharingService;
import de.qabel.desktop.config.factory.BlockBoxVolumeFactory;
import de.qabel.desktop.config.factory.BoxVolumeFactory;
import de.qabel.desktop.config.factory.DropUrlGenerator;
import de.qabel.desktop.config.factory.IdentityBuilderFactory;
import de.qabel.desktop.crashReports.CrashReportHandler;
import de.qabel.desktop.crashReports.HockeyApp;
import de.qabel.desktop.daemon.NetworkStatus;
import de.qabel.desktop.daemon.management.DefaultTransferManager;
import de.qabel.desktop.daemon.management.MonitoredTransferManager;
import de.qabel.desktop.daemon.management.TransferManager;
import de.qabel.desktop.inject.config.RuntimeConfiguration;
import de.qabel.desktop.ui.actionlog.item.renderer.MessageRendererFactory;
import de.qabel.desktop.ui.actionlog.item.renderer.PlaintextMessageRenderer;
import de.qabel.desktop.ui.connector.DropConnector;
import de.qabel.desktop.ui.connector.HttpDropConnector;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.UUID;

public abstract class RuntimeDesktopServiceFactory extends AnnotatedDesktopServiceFactory implements DesktopServices {
    protected RuntimeConfiguration runtimeConfiguration;
    private TransferManager transferManager;
    private IdentityBuilderFactory identityBuilderFactory;
    private DropUrlGenerator dropUrlGenerator;
    private NetworkStatus networkStatus = new NetworkStatus();
    private DropConnector dropConnector;
    private MessageRendererFactory messageRendererFactory;
    private SharingService sharingService;
    private BoxVolumeFactory boxVolumeFactory;
    private AccountingHTTP accountingHTTP;

    public RuntimeDesktopServiceFactory(RuntimeConfiguration runtimeConfiguration) {
        this.runtimeConfiguration = runtimeConfiguration;
    }

    @Override
    public synchronized TransferManager getTransferManager() {
        if (transferManager == null) {
            transferManager = new MonitoredTransferManager(new DefaultTransferManager());
        }
        return transferManager;
    }

    @Override
    public synchronized IdentityBuilderFactory getIdentityBuilderFactory() {
        if (identityBuilderFactory == null) {
            identityBuilderFactory = new IdentityBuilderFactory(getDropUrlGenerator());
        }
        return identityBuilderFactory;
    }

    @Override
    public synchronized DropUrlGenerator getDropUrlGenerator() {
        if (dropUrlGenerator == null) {
            dropUrlGenerator = new DropUrlGenerator(runtimeConfiguration.getDropUri());
        }
        return dropUrlGenerator;
    }

    @Deprecated
    protected String generateNewDeviceId() {
        return UUID.randomUUID().toString();
    }

    @Override
    public NetworkStatus getNetworkStatus() {
        return networkStatus;
    }

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

    @Override
    public synchronized MessageRendererFactory getDropMessageRendererFactory() {
        if (messageRendererFactory == null) {
            messageRendererFactory = new MessageRendererFactory();
            messageRendererFactory.setFallbackRenderer(new PlaintextMessageRenderer());
        }
        return messageRendererFactory;
    }

    @Override
    public synchronized SharingService getSharingService() {
        if (sharingService == null) {
            sharingService = new BlockSharingService(getDropMessageRepository(), getDropConnector());
        }
        return sharingService;
    }

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

    private ResourceBundle resourceBundle;
    @Override
    public synchronized ResourceBundle getResourceBundle() {
        if (resourceBundle == null) {
            resourceBundle = ResourceBundle.getBundle("ui", Locale.getDefault(), new UTF8Converter());
        }
        return resourceBundle;
    }
}
