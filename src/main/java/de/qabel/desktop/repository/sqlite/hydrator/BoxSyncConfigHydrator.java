package de.qabel.desktop.repository.sqlite.hydrator;

import de.qabel.core.config.Account;
import de.qabel.core.config.Identity;
import de.qabel.desktop.config.BoxSyncConfig;
import de.qabel.desktop.config.DefaultBoxSyncConfig;
import de.qabel.desktop.config.factory.BoxSyncConfigFactory;
import de.qabel.desktop.nio.boxfs.BoxFileSystem;
import de.qabel.desktop.nio.boxfs.BoxPath;
import de.qabel.desktop.repository.AccountRepository;
import de.qabel.desktop.repository.EntityManager;
import de.qabel.desktop.repository.IdentityRepository;
import de.qabel.desktop.repository.exception.EntityNotFoundExcepion;
import de.qabel.desktop.repository.exception.PersistenceException;
import de.qabel.desktop.repository.sqlite.Hydrator;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;

public class BoxSyncConfigHydrator extends AbstractHydrator<BoxSyncConfig> {
    private final EntityManager em;
    private final BoxSyncConfigFactory boxSyncConfigFactory;
    private final IdentityRepository identityRepo;
    private final AccountRepository accountRepo;

    public BoxSyncConfigHydrator(
        EntityManager em,
        BoxSyncConfigFactory boxSyncConfigFactory,
        IdentityRepository identityRepo,
        AccountRepository accountRepo
    ) {
        this.em = em;
        this.boxSyncConfigFactory = boxSyncConfigFactory;
        this.identityRepo = identityRepo;
        this.accountRepo = accountRepo;
    }

    @Override
    protected String[] getFields() {
        return new String[]{"id", "name", "identity_id", "account_id", "local_path", "remote_path", "paused"};
    }

    @Override
    public BoxSyncConfig hydrateOne(ResultSet resultSet) throws SQLException {
        int i = 1;
        int id = resultSet.getInt(i++);
        if (em.contains(BoxSyncConfig.class, id)) {
            return em.get(BoxSyncConfig.class, id);
        }

        String name = resultSet.getString(i++);
        int identityId = resultSet.getInt(i++);
        int accountId = resultSet.getInt(i++);
        Path localPath = Paths.get(resultSet.getString(i++));
        BoxPath remotePath = BoxFileSystem.get(resultSet.getString(i++));
        boolean paused = resultSet.getBoolean(i++);

        try {
            Identity identity = identityRepo.find(identityId);
            Account account = accountRepo.find(accountId);

            BoxSyncConfig config = boxSyncConfigFactory.createConfig(name, identity, account, localPath, remotePath);
            config.setId(id);
            if (paused) {
                config.pause();
            }
            recognize(config);
            return config;
        } catch (EntityNotFoundExcepion | PersistenceException e) {
            throw new SQLException("failed to load relations of boxSyncConfig: " + e.getMessage(), e);
        }
    }

    @Override
    public void recognize(BoxSyncConfig instance) {
        em.put(BoxSyncConfig.class, instance);
    }
}
