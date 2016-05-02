package de.qabel.desktop.repository.persistence;

import de.qabel.core.config.Account;
import de.qabel.core.config.Identity;
import de.qabel.core.config.Persistence;
import de.qabel.desktop.config.BoxSyncConfig;
import de.qabel.desktop.config.ClientConfiguration;
import de.qabel.desktop.config.DefaultBoxSyncConfig;
import de.qabel.desktop.config.factory.ClientConfigurationFactory;
import de.qabel.desktop.daemon.sync.worker.index.memory.InMemorySyncIndexFactory;
import de.qabel.desktop.nio.boxfs.BoxFileSystem;
import de.qabel.desktop.repository.AccountRepository;
import de.qabel.desktop.repository.ClientConfigurationRepository;
import de.qabel.desktop.repository.IdentityRepository;
import de.qabel.desktop.repository.exception.EntityNotFoundExcepion;
import de.qabel.desktop.repository.exception.PersistenceException;
import javafx.collections.ObservableList;

import javax.inject.Inject;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;

public class PersistenceClientConfigurationRepository extends AbstractPersistenceRepository implements ClientConfigurationRepository {
    private ClientConfigurationFactory fallbackConfigFactory;
    private AccountRepository accountRepository;
    private IdentityRepository identityRepository;

    @Inject
    public PersistenceClientConfigurationRepository(
            Persistence<String> persistence,
            ClientConfigurationFactory fallbackConfigFactory,
            IdentityRepository identityRepository,
            AccountRepository accountRepository
    ) {
        super(persistence);
        this.fallbackConfigFactory = fallbackConfigFactory;
        this.accountRepository = accountRepository;
        this.identityRepository = identityRepository;
    }

    @Override
    public ClientConfiguration load() {
        List<PersistentClientConfiguration> configs = persistence.getEntities(PersistentClientConfiguration.class);
        ClientConfiguration config = fallbackConfigFactory.createClientConfiguration();
        if (!configs.isEmpty()) {
            PersistentClientConfiguration configDto = configs.get(0);
            if (configDto.accountId != null) {
                try {
                    config.setAccount(accountRepository.find(configDto.accountId));
                } catch (EntityNotFoundExcepion entityNotFoundExcepion) {
                    entityNotFoundExcepion.printStackTrace();
                }
            }
            if (configDto.identitiyId != null) {
                try {
                    config.selectIdentity(identityRepository.find(configDto.identitiyId));
                } catch (EntityNotFoundExcepion | PersistenceException entityNotFoundExcepion) {
                    entityNotFoundExcepion.printStackTrace();
                }
            }

            ObservableList<BoxSyncConfig> boxSyncConfigs = config.getBoxSyncConfigs();
            if (configDto.boxSyncConfigs != null) {
                loadBoxSyncConfigs(configDto, boxSyncConfigs);
            }
            config.setLastDropMap(configDto.lastDropMap);
            config.setDeviceId(configDto.deviceId);
            config.setShareNotifications(configDto.shareNotifications == null ? new HashMap<>() : configDto.shareNotifications);
        }
        return config;
    }

    protected void loadBoxSyncConfigs(PersistentClientConfiguration configDto, ObservableList<BoxSyncConfig> boxSyncConfigs) {
        for (PersistentBoxSyncConfig dto : configDto.boxSyncConfigs) {
            try {
                DefaultBoxSyncConfig boxSyncConfig = new DefaultBoxSyncConfig(
                        dto.name,
                        Paths.get(dto.localPath),
                        BoxFileSystem.get(dto.remotePath),
                        identityRepository.find(dto.identity),
                        accountRepository.find(dto.account),
                        new InMemorySyncIndexFactory()
                );
                if (dto.syncIndex != null) {
                    boxSyncConfig.setSyncIndex(dto.syncIndex);
                }
                boxSyncConfigs.add(boxSyncConfig);
            } catch (EntityNotFoundExcepion | PersistenceException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void save(ClientConfiguration configuration) {
        PersistentClientConfiguration configDto;
        List<PersistentClientConfiguration> configs = persistence.getEntities(PersistentClientConfiguration.class);
        if (configs.isEmpty()) {
            configDto = new PersistentClientConfiguration();
        } else {
            configDto = configs.get(0);
        }
        configDto.accountId = configuration.hasAccount() ? configuration.getAccount().getPersistenceID() : null;
        configDto.identitiyId = configuration.getSelectedIdentity() != null ? configuration.getSelectedIdentity().getKeyIdentifier() : null;

        configDto.boxSyncConfigs.clear();
        for (BoxSyncConfig boxSyncConfig : configuration.getBoxSyncConfigs()) {
            configDto.boxSyncConfigs.add(createPersistentBoxSyncConfg(boxSyncConfig));
        }

        Account account = configuration.getAccount();
        if (account != null) {
            try {
                accountRepository.save(account);
                configDto.accountId = account.getPersistenceID();
            } catch (PersistenceException e) {
                throw new IllegalStateException("Failed to save account " + account);
            }
        }

        Identity identity = configuration.getSelectedIdentity();
        if (identity != null) {
            try {
                try {
                    identityRepository.find(identity.getPersistenceID());
                } catch (EntityNotFoundExcepion e) {
                    identityRepository.save(identity);
                }
                configDto.identitiyId = identity.getKeyIdentifier();
            } catch (PersistenceException e) {
                e.printStackTrace();
            }
        }
        configDto.lastDropMap = configuration.getLastDropMap();
        configDto.deviceId = configuration.getDeviceId();
        configDto.shareNotifications = configuration.getShareNotifications();

        persistence.updateOrPersistEntity(configDto);
    }

    private PersistentBoxSyncConfig createPersistentBoxSyncConfg(BoxSyncConfig boxSyncConfig) {
        PersistentBoxSyncConfig dto = new PersistentBoxSyncConfig();
        dto.name = boxSyncConfig.getName();
        dto.account = boxSyncConfig.getAccount().getPersistenceID();
        dto.identity = boxSyncConfig.getIdentity().getKeyIdentifier();
        dto.localPath = boxSyncConfig.getLocalPath().toString();
        dto.remotePath = boxSyncConfig.getRemotePath().toString();
        dto.syncIndex = boxSyncConfig.getSyncIndex();
        return dto;
    }
}
