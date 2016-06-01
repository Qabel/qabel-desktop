package de.qabel.desktop.inject;

import de.qabel.core.accounting.AccountingHTTP;
import de.qabel.desktop.SharingService;
import de.qabel.desktop.config.ClientConfig;
import de.qabel.desktop.config.factory.BoxSyncConfigFactory;
import de.qabel.desktop.config.factory.BoxVolumeFactory;
import de.qabel.desktop.config.factory.DropUrlGenerator;
import de.qabel.desktop.config.factory.IdentityBuilderFactory;
import de.qabel.desktop.crashReports.CrashReportHandler;
import de.qabel.desktop.daemon.NetworkStatus;
import de.qabel.desktop.daemon.drop.DropDaemon;
import de.qabel.desktop.daemon.management.TransferManager;
import de.qabel.desktop.daemon.sync.SyncDaemon;
import de.qabel.desktop.repository.*;
import de.qabel.desktop.ui.actionlog.item.renderer.FXMessageRendererFactory;
import de.qabel.desktop.ui.connector.DropConnector;
import de.qabel.desktop.util.Translator;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.ResourceBundle;

public interface DesktopServices {
    @Create(name = "loadManager")
    @Create(name = "transferManager")
    TransferManager getTransferManager();

    @Create(name = "identityRepository")
    IdentityRepository getIdentityRepository();

    @Create(name = "identityBuilderFactory")
    IdentityBuilderFactory getIdentityBuilderFactory();

    @Create(name = "accountingRepository")
    AccountRepository getAccountRepository();

    @Create(name = "dropUrlGenerator")
    DropUrlGenerator getDropUrlGenerator();

    @Create(name = "contactRepository")
    ContactRepository getContactRepository();

    @Create(name = "dropMessageRepository")
    DropMessageRepository getDropMessageRepository();

    @Create(name = "clientConfiguration")
    @Create(name = "config")
    ClientConfig getClientConfiguration();

    @Create(name = "networkStatus")
    NetworkStatus getNetworkStatus();

    @Create(name = "dropConnector")
    DropConnector getDropConnector();

    @Create(name = "reportHandler")
    CrashReportHandler getCrashReportHandler();

    @Create(name = "messageRendererFactory")
    FXMessageRendererFactory getDropMessageRendererFactory();

    @Create(name = "sharingService")
    SharingService getSharingService();

    @Create(name = "boxVolumeFactory")
    BoxVolumeFactory getBoxVolumeFactory() throws IOException;

    @Create(name = "accountingClient")
    AccountingHTTP getAccountingClient();

    @Create(name = "shareNotificationRepository")
    ShareNotificationRepository getShareNotificationRepository();

    @Create(name = "boxSyncConfigRepository")
    @Create(name = "boxSyncRepository")
    BoxSyncRepository getBoxSyncConfigRepository();

    @Create(name = "primaryStage")
    Stage getPrimaryStage();

    @Create(name = "transactionManager")
    TransactionManager getTransactionManager();

    @Create(name = "resourceBundle")
    ResourceBundle getResourceBundle();

    @Create(name = "translator")
    Translator getTranslator();

    @Create(name = "syncDaemon")
    SyncDaemon getSyncDaemon();

    @Create(name = "dropDaemon")
    DropDaemon getDropDaemon();

    @Create(name = "boxSyncConfigFactory")
    BoxSyncConfigFactory getBoxSyncConfigFactory();

    @Create(name="layoutWindow")
    Pane getLayoutWindow();


}
