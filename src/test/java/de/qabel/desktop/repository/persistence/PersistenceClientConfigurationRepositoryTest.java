package de.qabel.desktop.repository.persistence;

import de.qabel.core.config.Account;
import de.qabel.core.config.Identity;
import de.qabel.core.config.Persistence;
import de.qabel.desktop.config.BoxSyncConfig;
import de.qabel.desktop.config.ClientConfiguration;
import de.qabel.desktop.config.DefaultBoxSyncConfig;
import de.qabel.desktop.config.factory.ClientConfigurationFactory;
import de.qabel.desktop.config.factory.DropUrlGenerator;
import de.qabel.desktop.config.factory.IdentityBuilder;
import de.qabel.desktop.daemon.sync.worker.index.memory.InMemorySyncIndexFactory;
import de.qabel.desktop.nio.boxfs.BoxFileSystem;
import de.qabel.desktop.nio.boxfs.BoxPath;
import de.qabel.desktop.repository.AccountRepository;
import de.qabel.desktop.repository.IdentityRepository;
import de.qabel.desktop.repository.exception.PersistenceException;
import org.junit.Test;

import java.nio.file.Path;
import java.nio.file.Paths;

import static org.junit.Assert.*;

public class PersistenceClientConfigurationRepositoryTest extends AbstractPersistenceRepositoryTest<PersistenceClientConfigurationRepository> {
    private IdentityRepository identityRepo;
    private AccountRepository accountRepo;
    private Identity identity;

    @Override
    public void setUp() throws Exception {
        super.setUp();
        identity = new IdentityBuilder(new DropUrlGenerator("http://localhost:5000")).withAlias("wayne").build();
        for (PersistentClientConfiguration config : persistence.getEntities(PersistentClientConfiguration.class)) {
            persistence.removeEntity(config.getPersistenceID(), PersistentClientConfiguration.class);
        }
    }

    @Override
    protected PersistenceClientConfigurationRepository createRepository(Persistence<String> persistence) {
        identityRepo = new PersistenceIdentityRepository(persistence);
        accountRepo = new PersistenceAccountRepository(persistence);

        return new PersistenceClientConfigurationRepository(
                persistence,
                new ClientConfigurationFactory(),
                identityRepo,
                accountRepo
        );
    }

    @Test
    public void createsNewConfigByDefault() {
        assertNotNull(repo.load());
    }

    @Test
    public void storesConfig() {
        ClientConfiguration config = new ClientConfigurationFactory().createClientConfiguration();
        Account account = new Account("a", "b", "c");
        config.setAccount(account);
        config.selectIdentity(identity);
        config.setDeviceId("dev id");

        repo.save(config);

        ClientConfiguration loadedConfig = repo.load();
        assertEquals(identity, loadedConfig.getSelectedIdentity());
        assertEquals(account, loadedConfig.getAccount());
        assertTrue(loadedConfig.hasDeviceId());
        assertEquals("dev id", loadedConfig.getDeviceId());
    }

    @Test
    public void holdsReferenceToNewestEntities() throws PersistenceException {
        ClientConfiguration config = new ClientConfigurationFactory().createClientConfiguration();
        Account account = new Account("a", "b", "c");
        config.setAccount(account);
        config.selectIdentity(identity);

        repo.save(config);

        identity.setAlias("new alias");
        account.setUser("someone else");

        identityRepo.save(identity);
        accountRepo.save(account);

        config = repo.load();
        assertSame(identity, config.getSelectedIdentity());
        assertSame(account, config.getAccount());
    }

    @Test
    public void persistsBoxSyncConfig() throws Exception {
        ClientConfiguration config = new ClientConfigurationFactory().createClientConfiguration();
        Account account = new Account("a", "b", "c");

        identityRepo.save(identity);
        accountRepo.save(account);

        Path localPath = Paths.get("/tmp/some/where");
        BoxPath remotePath = BoxFileSystem.get("over/the/rainbow");
        DefaultBoxSyncConfig boxSyncConfig = new DefaultBoxSyncConfig(
            "named",
            localPath,
            remotePath,
            identity,
            account,
            new InMemorySyncIndexFactory()
        );
        config.getBoxSyncConfigs().add(boxSyncConfig);

        repo.save(config);
        config = repo.load();
        repo.save(config);
        config = repo.load();

        assertEquals(1, config.getBoxSyncConfigs().size());
        BoxSyncConfig boxConfig = config.getBoxSyncConfigs().get(0);

        assertSame(identity, boxConfig.getIdentity());
        assertEquals(account, boxConfig.getAccount());
        assertEquals(account.hashCode(), boxConfig.getAccount().hashCode());
        assertEquals(Paths.get("/tmp/some/where").toString(), boxConfig.getLocalPath().toString());
        assertEquals("/over/the/rainbow", boxConfig.getRemotePath().toString());
        assertEquals("named", boxConfig.getName());
    }
}
