package de.qabel.desktop.repository.persistence;

import de.qabel.core.config.Account;
import de.qabel.core.config.Identity;
import de.qabel.core.config.Persistence;
import de.qabel.desktop.config.BoxSyncConfig;
import de.qabel.desktop.config.ClientConfiguration;
import de.qabel.desktop.config.DefaultBoxSyncConfig;
import de.qabel.desktop.config.DefaultClientConfiguration;
import de.qabel.desktop.config.factory.ClientConfigurationFactory;
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

	@Override
	public void setUp() throws Exception {
		super.setUp();
		for (PersistentClientConfiguration config : persistence.getEntities(PersistentClientConfiguration.class)) {
			persistence.removeEntity(config.getPersistenceID(), PersistentClientConfiguration.class);
		}
	}

	@Override
	protected PersistenceClientConfigurationRepository createRepository(Persistence persistence) {
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
		Identity identity = new Identity("alias", null, null);
		config.setAccount(account);
		config.selectIdentity(identity);

		repo.save(config);

		ClientConfiguration loadedConfig = repo.load();
		assertEquals(identity, loadedConfig.getSelectedIdentity());
		assertEquals(account, loadedConfig.getAccount());
	}

	@Test
	public void holdsReferenceToNewestEntities() throws PersistenceException {
		ClientConfiguration config = new ClientConfigurationFactory().createClientConfiguration();
		Account account = new Account("a", "b", "c");
		Identity identity = new Identity("alias", null, null);
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
		Identity identity = new Identity("alias", null, null);

		identityRepo.save(identity);
		accountRepo.save(account);

		Path localPath = Paths.get("some/where");
		Path remotePath = Paths.get("over/the/rainbow");
		config.getBoxSyncConfigs().add(new DefaultBoxSyncConfig(localPath, remotePath, identity, account));

		repo.save(config);
		config = repo.load();

		assertEquals(1, config.getBoxSyncConfigs().size());
		BoxSyncConfig boxConfig = config.getBoxSyncConfigs().get(0);

		assertSame(identity, boxConfig.getIdentity());
		assertSame(account, boxConfig.getAccount());
		assertEquals("some/where", boxConfig.getLocalPath().toString());
		assertEquals("over/the/rainbow", boxConfig.getRemotePath().toString());
	}
}
