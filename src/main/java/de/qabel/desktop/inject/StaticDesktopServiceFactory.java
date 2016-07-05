package de.qabel.desktop.inject;

import de.qabel.desktop.config.ClientConfig;
import de.qabel.desktop.config.factory.BoxSyncConfigFactory;
import de.qabel.desktop.config.factory.DefaultBoxSyncConfigFactory;
import de.qabel.desktop.daemon.sync.worker.index.memory.InMemorySyncIndexFactory;
import de.qabel.desktop.inject.config.RuntimeConfiguration;
import de.qabel.desktop.repository.*;

public abstract class StaticDesktopServiceFactory extends RuntimeDesktopServiceFactory {

    public StaticDesktopServiceFactory(RuntimeConfiguration runtimeConfiguration) {
        super(runtimeConfiguration);
    }

    protected IdentityRepository identityRepository;

    AccountRepository accountRepository;

    protected ClientConfig clientConfiguration;

    private ClientConfigurationRepository clientConfigurationRepository;

    public abstract BoxSyncRepository getBoxSyncConfigRepository();

    public abstract ShareNotificationRepository getShareNotificationRepository();

    @Override
    public BoxSyncConfigFactory getBoxSyncConfigFactory() {
        return new DefaultBoxSyncConfigFactory(new InMemorySyncIndexFactory());
    }
}
