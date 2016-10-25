package de.qabel.desktop.repository.sqlite;

import de.qabel.core.config.Identity;
import de.qabel.core.config.factory.DropUrlGenerator;
import de.qabel.core.config.factory.IdentityBuilder;
import de.qabel.core.repository.EntityManager;
import de.qabel.core.repository.IdentityRepository;
import de.qabel.core.repository.exception.PersistenceException;
import de.qabel.core.repository.sqlite.ClientDatabase;
import de.qabel.core.repository.sqlite.SqliteDropUrlRepository;
import de.qabel.core.repository.sqlite.SqliteIdentityRepository;
import de.qabel.core.repository.sqlite.SqlitePrefixRepository;
import de.qabel.core.repository.sqlite.hydrator.DropURLHydrator;
import de.qabel.desktop.daemon.drop.ShareNotificationMessage;
import de.qabel.desktop.repository.ShareNotificationRepository;
import org.junit.Test;
import org.spongycastle.util.encoders.Hex;

import java.util.LinkedList;
import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

public class SqliteShareNotificationRepositoryTest extends AbstractSqliteRepositoryTest<ShareNotificationRepository> {
    private List<ShareNotificationMessage> adds = new LinkedList<>();
    private List<ShareNotificationMessage> deletes = new LinkedList<>();
    private Identity identity;
    private Identity otherIdentity;
    private IdentityRepository identityRepo;
    private String key = Hex.toHexString("testkey".getBytes());

    @Override
    public void setUp() throws Exception {
        super.setUp();
        identity = new IdentityBuilder(new DropUrlGenerator("http://localhost")).withAlias("test").build();
        otherIdentity = new IdentityBuilder(new DropUrlGenerator("http://localhost")).withAlias("test2").build();
        identityRepo = new SqliteIdentityRepository(
            clientDatabase, em,
            new SqlitePrefixRepository(clientDatabase),
            new SqliteDropUrlRepository(clientDatabase, new DropURLHydrator())
        );
        identityRepo.save(identity);

        repo.onAdd(adds::add, identity);
        repo.onDelete(deletes::add, identity);
    }

    @Override
    protected ShareNotificationRepository createRepo(ClientDatabase clientDatabase, EntityManager em) throws Exception {
        return new SqliteShareNotificationRepository(clientDatabase, em);
    }

    @Test
    public void findsSavedNotifications() throws PersistenceException {
        ShareNotificationMessage message = new ShareNotificationMessage("url", key, "hey dude");
        repo.save(identity, message);

        List<ShareNotificationMessage> shares = repo.find(identity);
        assertEquals(1, shares.size());
        assertSame(message, shares.get(0));
        assertEquals(1, adds.size());
        assertSame(message, adds.get(0));
    }

    @Test
    public void findsUncachedNotifications() throws PersistenceException {
        ShareNotificationMessage message = new ShareNotificationMessage("url", key, "hey dude");
        repo.save(identity, message);
        em.clear();

        List<ShareNotificationMessage> shares = repo.find(identity);
        assertEquals(1, shares.size());
        ShareNotificationMessage share = shares.get(0);
        assertEquals("url", share.getUrl());
        assertEquals(key, Hex.toHexString(share.getKey().getKey()));
        assertEquals("hey dude", share.getMsg());
        assertSame(share, repo.find(identity).get(0));

        assertThat(repo.find(otherIdentity), is(empty()));
    }

    @Test
    public void ignoresSavedEntity() throws PersistenceException {
        ShareNotificationMessage message = new ShareNotificationMessage("url", key, "hey dude");
        repo.save(identity, message);
        em.clear();

        repo.save(identity, message);
        assertEquals(1, repo.find(identity).size());
    }

    @Test
    public void deletesShares() throws Exception {
        ShareNotificationMessage message = new ShareNotificationMessage("url", key, "yo");
        repo.save(identity, message);

        repo.delete(message);

        assertThat(repo.find(identity), is(empty()));
        assertThat(deletes, is(not(empty())));
        assertThat(deletes, hasItem(message));
    }
}
