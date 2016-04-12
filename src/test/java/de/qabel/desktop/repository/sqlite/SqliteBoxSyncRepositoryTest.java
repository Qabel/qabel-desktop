package de.qabel.desktop.repository.sqlite;

import de.qabel.core.config.Account;
import de.qabel.core.config.Identity;
import de.qabel.desktop.config.BoxSyncConfig;
import de.qabel.desktop.config.DefaultBoxSyncConfig;
import de.qabel.desktop.config.factory.DropUrlGenerator;
import de.qabel.desktop.config.factory.IdentityBuilder;
import de.qabel.desktop.repository.EntityManager;
import org.junit.Test;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.LinkedList;
import java.util.List;

import static org.junit.Assert.*;

public class SqliteBoxSyncRepositoryTest extends AbstractSqliteRepositoryTest<SqliteBoxSyncRepository> {
    private List<BoxSyncConfig> adds = new LinkedList<>();
    private List<BoxSyncConfig> deletes = new LinkedList<>();
    private SqliteAccountRepository accountRepo;
    private SqliteIdentityRepository identityRepo;
    private Account account;
    private Identity identity;
    private BoxSyncConfig config;

    @Override
    protected SqliteBoxSyncRepository createRepo(ClientDatabase clientDatabase, EntityManager em) throws Exception {
        return new SqliteBoxSyncRepository(clientDatabase, em);
    }

    @Override
    public void setUp() throws Exception {
        super.setUp();
        repo.onAdd(adds::add);
        repo.onDelete(deletes::add);

        accountRepo = new SqliteAccountRepository(clientDatabase, em);
        identityRepo = new SqliteIdentityRepository(clientDatabase, em);
        account = new Account("p", "u", "a");
        identity = new IdentityBuilder(new DropUrlGenerator("http://localhost")).withAlias("test").build();

        accountRepo.save(account);
        identityRepo.save(identity);


        Path localPath = Paths.get("/tmp/wayne");
        Path remotePath = Paths.get("/tmp/remoteWayne");
        config = new DefaultBoxSyncConfig("testC", localPath, remotePath, identity, account);
    }

    @Test
    public void findsSavedConfig() throws Exception {
        repo.save(config);

        List<BoxSyncConfig> configs = repo.findAll();
        assertEquals(1, configs.size());
        assertSame(config, configs.get(0));
        assertEquals(1, adds.size());
        assertSame(config, adds.get(0));
    }

    @Test
    public void findsUncachedConfigs() throws Exception {
        repo.save(config);
        em.clear();
        em.put(Identity.class, identity);
        em.put(Account.class, account);

        List<BoxSyncConfig> configs = repo.findAll();
        assertEquals(1, configs.size());
        BoxSyncConfig loaded = configs.get(0);
        assertEquals("/tmp/wayne", loaded.getLocalPath().toString());
        assertEquals("/tmp/remoteWayne", loaded.getRemotePath().toString());
        assertEquals("testC", loaded.getName());
        assertSame(identity, loaded.getIdentity());
        assertSame(account, loaded.getAccount());
        assertSame(loaded, repo.findAll().get(0));
    }

    @Test
    public void updatesExistingConfigs() throws Exception {
        repo.save(config);
        config.setName("new name");
        repo.save(config);
        em.clear();

        List<BoxSyncConfig> configs = repo.findAll();
        assertEquals(1, configs.size());
        BoxSyncConfig loaded = configs.get(0);
        assertEquals("new name", loaded.getName());
        assertEquals("/tmp/wayne", loaded.getLocalPath().toString());
        assertEquals("/tmp/remoteWayne", loaded.getRemotePath().toString());
        assertEquals(identity.getId(), loaded.getIdentity().getId());
        assertEquals(account.getId(), loaded.getAccount().getId());
    }

    @Test
    public void deletesConfigs() throws Exception {
        repo.save(config);
        repo.delete(config);

        assertEquals(0, repo.findAll().size());
        assertEquals(1, deletes.size());
        assertSame(config, deletes.get(0));
        repo.save(config);
        assertEquals(1, repo.findAll().size());
    }
}
