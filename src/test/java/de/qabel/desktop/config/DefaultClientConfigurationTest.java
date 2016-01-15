package de.qabel.desktop.config;

import de.qabel.core.config.Account;
import de.qabel.core.config.Identity;
import de.qabel.desktop.ui.sync.DummyBoxSyncConfig;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

public class DefaultClientConfigurationTest {
	private boolean[] updated;
	private DefaultClientConfiguration configuration;

	@Before
	public void setUp() throws Exception {
		updated = new boolean[]{false};
		configuration = new DefaultClientConfiguration();
		configuration.addObserver((o, arg) -> updated[0] = true);
	}

	private Account account() {
		return new Account("a", "b", "c");
	}

	@Test
	public void knowsWhenAccountIsMissing() throws Exception {
		assertFalse(configuration.hasAccount());
		assertFalse(wasUpdated());
	}

	private boolean wasUpdated() {
		return updated[0];
	}

	@Test
	public void knowsWhenAccountIsSet() throws Exception {
		Account account = account();
		configuration.setAccount(account);

		assertTrue("account was not set", configuration.hasAccount());
		assertSame("account could not be received", account, configuration.getAccount());
		assertTrue("setAccount did not trigger an update", wasUpdated());
	}

	@Test(expected = IllegalStateException.class)
	public void doesNotAcceptAccountChanges() throws Exception {
		configuration.setAccount(account());
		configuration.setAccount(account());
	}

	@Test
	public void handlesIdentityUpdates() throws Exception {
		Identity identity = identity();
		configuration.selectIdentity(identity);

		assertSame("identity not set correctly", identity, configuration.getSelectedIdentity());
		assertTrue("selectIdentity did not trigger update", wasUpdated());
	}

	@Test
	public void onDuplicateSelectionDoesNotUpdate() {
		Identity identity = identity();
		configuration.selectIdentity(identity);

		resetUpdates();
		configuration.selectIdentity(identity);

		assertFalse("selectIdentity triggered update even though identity was not changed", wasUpdated());
	}

	@Test
	public void notifiesOnSyncConfigChange() {
		configuration.getBoxSyncConfigs().add(new DummyBoxSyncConfig());
		assertTrue("boxSyncConfig change did not trigger update", wasUpdated());
	}

	private void resetUpdates() {
		updated[0] = false;
	}

	private Identity identity() {
		return new Identity("alias", null, null);
	}
}