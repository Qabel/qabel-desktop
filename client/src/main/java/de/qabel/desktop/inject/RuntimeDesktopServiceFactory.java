package de.qabel.desktop.inject;

import com.google.common.io.Files;
import de.qabel.box.storage.DirectoryMetadataFactory;
import de.qabel.box.storage.factory.BlockBoxVolumeFactory;
import de.qabel.box.storage.factory.BoxVolumeFactory;
import de.qabel.box.storage.factory.CachedBoxVolumeFactory;
import de.qabel.box.storage.jdbc.JdbcDirectoryMetadataFactory;
import de.qabel.core.accounting.AccountingProfile;
import de.qabel.core.accounting.BoxClient;
import de.qabel.core.accounting.BoxHttpClient;
import de.qabel.core.config.Account;
import de.qabel.core.config.AccountingServer;
import de.qabel.core.config.factory.DropUrlGenerator;
import de.qabel.core.config.factory.IdentityBuilderFactory;
import de.qabel.core.http.DropHTTP;
import de.qabel.core.index.server.IndexHTTP;
import de.qabel.core.index.server.IndexHTTPLocation;
import de.qabel.core.index.server.IndexServer;
import de.qabel.core.repository.exception.PersistenceException;
import de.qabel.desktop.BlockSharingService;
import de.qabel.desktop.SharingService;
import de.qabel.desktop.config.BoxSyncConfig;
import de.qabel.desktop.config.FilesAbout;
import de.qabel.desktop.crashReports.CrashReportHandler;
import de.qabel.desktop.daemon.NetworkStatus;
import de.qabel.desktop.daemon.drop.DropDaemon;
import de.qabel.desktop.daemon.management.DefaultTransferManager;
import de.qabel.desktop.daemon.management.MonitoredTransferManager;
import de.qabel.desktop.daemon.management.TransferManager;
import de.qabel.desktop.daemon.sync.SyncDaemon;
import de.qabel.desktop.daemon.sync.worker.DefaultSyncerFactory;
import de.qabel.desktop.daemon.sync.worker.SyncerFactory;
import de.qabel.desktop.hockeyapp.*;
import de.qabel.desktop.inject.config.RuntimeConfiguration;
import de.qabel.desktop.repository.BoxSyncRepository;
import de.qabel.desktop.ui.actionlog.item.renderer.FXMessageRendererFactory;
import de.qabel.desktop.ui.actionlog.item.renderer.PlaintextMessageRenderer;
import de.qabel.desktop.ui.connector.DropConnector;
import de.qabel.desktop.ui.connector.HttpDropConnector;
import de.qabel.desktop.util.Translator;
import de.qabel.desktop.util.UTF8Converter;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;
import org.apache.http.impl.client.HttpClients;
import rx.Scheduler;
import rx.schedulers.JavaFxScheduler;
import rx.schedulers.Schedulers;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Locale;
import java.util.ResourceBundle;

public abstract class RuntimeDesktopServiceFactory extends AnnotatedDesktopServiceFactory implements DesktopServices {
    protected RuntimeConfiguration runtimeConfiguration;
    private TransferManager transferManager;
    private IdentityBuilderFactory identityBuilderFactory;
    private DropUrlGenerator dropUrlGenerator;
    private NetworkStatus networkStatus = new NetworkStatus();
    private DropConnector dropConnector;
    private FXMessageRendererFactory FXMessageRendererFactory;
    private SharingService sharingService;
    private BoxVolumeFactory boxVolumeFactory;
    private BoxClient boxClient;
    private IndexServer indexServer;

    public RuntimeDesktopServiceFactory(RuntimeConfiguration runtimeConfiguration) {
        this.runtimeConfiguration = runtimeConfiguration;
    }

    protected IndexServer getIndexServer() {
        if (indexServer == null) {
            indexServer = new IndexHTTP(
                new IndexHTTPLocation(runtimeConfiguration.getIndexUri()),
                HttpClients.createDefault()
            );
        }
        return indexServer;
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

    private HockeyApp hockeyApp;

    @Override
    public synchronized CrashReportHandler getCrashReportHandler() {
        if (hockeyApp == null) {
            String apiBaseUri = runtimeConfiguration.getCrashReportUri().toString();

            HockeyAppRequestBuilder requestBuilder = new HockeyAppRequestBuilder(apiBaseUri, getCurrentVersion(), HttpClients.createMinimal());
            VersionClient versionClient = new VersionClient(requestBuilder);
            HockeyFeedbackClient feedbackClient = new HockeyFeedbackClient(requestBuilder, versionClient);
            HockeyCrashReporterClient crashClient = new HockeyCrashReporterClient(requestBuilder, versionClient);

            hockeyApp = new HockeyApp(feedbackClient, crashClient);
        }
        return hockeyApp;
    }

    @Override
    public synchronized FXMessageRendererFactory getDropMessageRendererFactory() {
        if (FXMessageRendererFactory == null) {
            FXMessageRendererFactory = new FXMessageRendererFactory();
            FXMessageRendererFactory.setFallbackRenderer(new PlaintextMessageRenderer());
        }
        return FXMessageRendererFactory;
    }

    @Override
    public synchronized SharingService getSharingService() {
        if (sharingService == null) {
            sharingService = new BlockSharingService(getDropMessageRepository(), getDropConnector());
        }
        return sharingService;
    }

    private DirectoryMetadataFactory directoryMetadataFactory;
    public synchronized DirectoryMetadataFactory getDirectoryMetadataFactory() {
        if (directoryMetadataFactory == null) {
            directoryMetadataFactory = new JdbcDirectoryMetadataFactory(getTempDir(), getDeviceId());
        }
        return directoryMetadataFactory;
    }

    private File tmpDir = Files.createTempDir();

    private synchronized File getTempDir() {
        return tmpDir;
    }

    @Override
    public synchronized BoxVolumeFactory getBoxVolumeFactory() throws IOException {
        if (boxVolumeFactory == null) {
            boxVolumeFactory = new CachedBoxVolumeFactory(new BlockBoxVolumeFactory(
                getDeviceId(),
                getBoxClient(),
                getIdentityRepository(),
                getDirectoryMetadataFactory(),
                getBlockUri(),
                getTempDir()
            ));
        }
        return boxVolumeFactory;
    }

    private byte[] getDeviceId() {
        return getClientConfiguration().getDeviceId().getBytes();
    }

    @Override
    public synchronized BoxClient getBoxClient() {
        if (boxClient == null) {
            Account acc = getClientConfiguration().getAccount();
            if (acc == null) {
                throw new IllegalStateException("cannot get box client without valid account");
            }
            try {
                AccountingServer server = new AccountingServer(
                    new URI(acc.getProvider()),
                    getBlockUri(),
                    acc.getUser(),
                    acc.getAuth()
                );
                boxClient = new BoxHttpClient(server, new AccountingProfile());
            } catch (URISyntaxException e) {
                throw new IllegalStateException("cannot get box client without valid account: " + e.getMessage(), e);
            }
        }
        return boxClient;
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

    @Override
    public Translator getTranslator() {
        return new Translator(getResourceBundle());
    }

    private SyncDaemon syncDaemon;

    public SyncerFactory getSyncerFactory() throws IOException {
        return new DefaultSyncerFactory(getBoxVolumeFactory(), getTransferManager());
    }

    @Override
    public synchronized SyncDaemon getSyncDaemon() {
        if (syncDaemon == null) {
            try {
                BoxSyncRepository syncRepo = getBoxSyncConfigRepository();
                ObservableList<BoxSyncConfig> configs = FXCollections.observableList(syncRepo.findAll());
                syncRepo.onAdd(configs::add);
                syncDaemon = new SyncDaemon(configs, getSyncerFactory());
            } catch (PersistenceException | IOException e) {
                throw new IllegalStateException("failed to create sync daemon: " + e.getMessage(), e);
            }
        }
        return syncDaemon;
    }

    private DropDaemon dropDaemon;

    @Override
    public synchronized DropDaemon getDropDaemon() {
        if (dropDaemon == null) {
            dropDaemon = new DropDaemon(
                getChatService(),
                getDropMessageRepository(),
                getContactRepository(),
                getIdentityRepository(),
                getEventDispatcher()
            );
        }
        return dropDaemon;
    }

    public Pane getLayoutWindow() {
        return runtimeConfiguration.getWindow();
    }

    public FilesAbout getAboutFilesContent() {
        return runtimeConfiguration.getAboutFilesContent();
    }

    public String getCurrentVersion() {
        return runtimeConfiguration.getCurrentVersion();
    }

    @Override
    public Scheduler getFxScheduler() {
        return JavaFxScheduler.getInstance();
    }

    @Override
    public Scheduler getIoScheduler() {
        return Schedulers.io();
    }

    @Override
    public Scheduler getComputationScheduler() {
        return Schedulers.computation();
    }
}
