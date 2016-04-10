package de.qabel.desktop.config;

import de.qabel.core.config.Account;
import de.qabel.core.config.Identity;
import de.qabel.desktop.config.factory.DropUrlGenerator;
import de.qabel.desktop.config.factory.IdentityBuilder;
import de.qabel.desktop.daemon.drop.ShareNotificationMessage;
import de.qabel.desktop.repository.*;
import de.qabel.desktop.repository.inmemory.*;
import org.junit.Test;

import java.util.Date;
import java.util.List;
import java.util.Set;

import static org.junit.Assert.*;

public class RepositoryBasedClientConfigTest {
    private ClientConfigRepository clientConfigRepo = new InMemoryClientConfigRepository();
    private AccountRepository accountRepo = new InMemoryAccountRepository();
    private IdentityRepository identityRepo = new InMemoryIdentityRepository();
    private DropStateRepository dropStateRepo = new InMemoryDropStateRepository();
    private ShareNotificationRepository shareRepo = new InMemoryShareNotificationRepository();

    private RepositoryBasedClientConfig config = new RepositoryBasedClientConfig(
        clientConfigRepo,
        accountRepo,
        identityRepo,
        dropStateRepo,
        shareRepo
    );

    @Test
    public void savesAccount() throws Exception {
        assertFalse(config.hasAccount());

        Account account = new Account("p", "u", "a");
        config.setAccount(account);

        assertTrue(config.hasAccount());
        assertSame(account, config.getAccount());
        List<Account> accounts = accountRepo.findAll();
        assertEquals(1, accounts.size());
        assertSame(account, accounts.get(0));

        config.setAccount(account);
        assertEquals(1, accountRepo.findAll().size());
    }

    @Test
    public void savesIdentity() throws Exception {
        assertNull(config.getSelectedIdentity());

        Identity identity = new IdentityBuilder(new DropUrlGenerator("http://localhost")).withAlias("tester").build();
        config.selectIdentity(identity);

        assertSame(identity, config.getSelectedIdentity());
        Set<Identity> identities = identityRepo.findAll().getIdentities();
        assertEquals(1, identities.size());
        assertSame(identity, identities.toArray()[0]);

        config.selectIdentity(identity);
        assertEquals(1, identityRepo.findAll().getIdentities().size());
    }

    @Test
    public void savesDeviceId() throws Exception {
        assertFalse(config.hasDeviceId());

        config.setDeviceId("device");

        assertEquals("device", config.getDeviceId());
        assertTrue(config.hasDeviceId());
    }

    @Test
    public void savesDropPoll() throws Exception {
        Identity identity = new IdentityBuilder(new DropUrlGenerator("http://localhost")).withAlias("tester").build();
        Identity someone = new IdentityBuilder(new DropUrlGenerator("http://localhost")).withAlias("tester2").build();

        Date poll = new Date();
        config.setLastDropPoll(identity, poll);
        assertEquals(poll.getTime(), config.getLastDropPoll(identity).getTime());
        assertNull(config.getLastDropPoll(someone));
    }

    @Test
    public void knowsShares() throws Exception {
        Identity identity = new IdentityBuilder(new DropUrlGenerator("http://localhost")).withAlias("tester").build();
        ShareNotificationMessage share = new ShareNotificationMessage("url", "key", "msg");

        shareRepo.save(identity, share);

        ShareNotifications notifications = config.getShareNotification(identity);
        assertEquals(1, notifications.getNotifications().size());
        assertTrue(notifications.getNotifications().contains(share));
    }
}
