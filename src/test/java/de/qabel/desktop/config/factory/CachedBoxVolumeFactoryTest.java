package de.qabel.desktop.config.factory;

import de.qabel.core.config.Account;
import de.qabel.core.config.Identity;
import de.qabel.core.config.factory.DropUrlGenerator;
import de.qabel.core.config.factory.IdentityBuilderFactory;
import de.qabel.desktop.daemon.management.BoxVolumeFactoryStub;
import de.qabel.desktop.daemon.sync.worker.BoxVolumeStub;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertSame;

public class CachedBoxVolumeFactoryTest {

    private Account account;
    private Account account2;
    private Identity identity;
    private Identity identity2;
    private BoxVolumeFactoryStub factory;
    private BoxVolumeStub boxVolume1;

    @Before
    public void setUp() throws Exception {
        account = new Account("a", "b", "c");
        account2 = new Account("a2", "b2", "c2");
        identity = new IdentityBuilderFactory(new DropUrlGenerator("http://localhost")).factory().build();
        identity2 = new IdentityBuilderFactory(new DropUrlGenerator("http://localhost")).factory().build();
        factory = new BoxVolumeFactoryStub();
        boxVolume1 = new BoxVolumeStub();
        factory.boxVolume = boxVolume1;
    }

    @Test
    public void returnsSameFactoryForSameAccountAndIdentity() throws Exception {
        BoxVolumeFactory sut = new CachedBoxVolumeFactory(factory);
        assertSame(boxVolume1, sut.getVolume(account, identity));

        factory.boxVolume = new BoxVolumeStub();
        assertSame(boxVolume1, sut.getVolume(account, identity));
    }

    @Test
    public void retunsNewFactoryForAnotherAccount() throws Exception {
        BoxVolumeFactory sut = new CachedBoxVolumeFactory(factory);
        sut.getVolume(account, identity);

        factory.boxVolume = new BoxVolumeStub();
        assertNotSame(boxVolume1, sut.getVolume(account2, identity));
    }

    @Test
    public void returnsNewFactoryForAnotherIdentity() throws Exception {
        BoxVolumeFactory sut = new CachedBoxVolumeFactory(factory);
        sut.getVolume(account, identity);

        factory.boxVolume = new BoxVolumeStub();
        assertNotSame(boxVolume1, sut.getVolume(account, identity2));
    }
}
