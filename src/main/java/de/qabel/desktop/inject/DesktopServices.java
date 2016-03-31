package de.qabel.desktop.inject;

import de.qabel.core.accounting.AccountingHTTP;
import de.qabel.desktop.SharingService;
import de.qabel.desktop.config.ClientConfiguration;
import de.qabel.desktop.config.factory.BoxVolumeFactory;
import de.qabel.desktop.config.factory.DropUrlGenerator;
import de.qabel.desktop.config.factory.IdentityBuilderFactory;
import de.qabel.desktop.crashReports.CrashReportHandler;
import de.qabel.desktop.daemon.NetworkStatus;
import de.qabel.desktop.daemon.management.TransferManager;
import de.qabel.desktop.repository.*;
import de.qabel.desktop.ui.actionlog.item.renderer.MessageRendererFactory;
import de.qabel.desktop.ui.connector.DropConnector;

public interface DesktopServices {
    TransferManager getTransferManager();

    IdentityRepository getIdentityRepository();

    IdentityBuilderFactory getIdentityBuilderFactory();

    AccountRepository getAccountRepository();

    DropUrlGenerator getDropUrlGenerator();

    ContactRepository getContactRepository();

    DropMessageRepository getDropMessageRepository();

    ClientConfiguration getClientConfiguration();

    ClientConfigurationRepository getClientConfigurationRepository();

    NetworkStatus getNetworkStatus();

    DropConnector getDropConnector();

    CrashReportHandler getCrashReportHandler();

    MessageRendererFactory getDropMessageRendererFactory();

    SharingService getSharingService();

    BoxVolumeFactory getBoxVolumeFactory();

    AccountingHTTP getAccountingClient();
}
