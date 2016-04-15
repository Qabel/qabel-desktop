package de.qabel.desktop.inject;

import de.qabel.core.config.Persistence;
import de.qabel.core.config.SQLitePersistence;
import de.qabel.desktop.config.ClientConfig;
import de.qabel.desktop.config.factory.*;
import de.qabel.desktop.inject.config.RuntimeConfiguration;
import de.qabel.desktop.repository.*;
import de.qabel.desktop.repository.persistence.*;

public abstract class StaticDesktopServiceFactory extends RuntimeDesktopServiceFactory {

    public StaticDesktopServiceFactory(RuntimeConfiguration runtimeConfiguration) {
        super(runtimeConfiguration);
    }

    protected IdentityRepository identityRepository;

    @Override
    public synchronized IdentityRepository getIdentityRepository() {
        if (identityRepository == null) {
            identityRepository = new PersistenceIdentityRepository(getPersistence());
        }
        return identityRepository;
    }

    AccountRepository accountRepository;

    @Override
    public synchronized AccountRepository getAccountRepository() {
        if (accountRepository == null) {
            accountRepository = new PersistenceAccountRepository(getPersistence());
        }
        return accountRepository;
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

    protected DropMessageRepository dropMessageRepository;

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

    private ClientConfigurationRepository clientConfigurationRepository;

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

    public abstract BoxSyncRepository getBoxSyncConfigRepository();

    public abstract ShareNotificationRepository getShareNotificationRepository();
}
