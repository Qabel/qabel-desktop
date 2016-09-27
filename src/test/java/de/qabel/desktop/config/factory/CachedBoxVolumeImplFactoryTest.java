package de.qabel.desktop.config.factory;

import de.qabel.core.config.Account;
import de.qabel.core.config.Identity;
import de.qabel.core.config.factory.DropUrlGenerator;
import de.qabel.core.config.factory.IdentityBuilderFactory;
import de.qabel.desktop.daemon.sync.worker.BoxVolumeStub;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertSame;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;

public class CachedBoxVolumeImplFactoryTest {

    private Account account;
    private Account account2;
    private Identity identity;
    private Identity identity2;
    private BoxVolumeFactory factory;
    private BoxVolumeStub boxVolume1;

    @Before
    public void setUp() throws Exception {
        account = new Account("a", "b", "c");
        account2 = new Account("a2", "b2", "c2");
        identity = new IdentityBuilderFactory(new DropUrlGenerator("http://localhost")).factory().withAlias("foo").build();
        identity2 = new IdentityBuilderFactory(new DropUrlGenerator("http://localhost")).factory().withAlias("foo").build();
        factory = mock(BoxVolumeFactory.class);
        boxVolume1 = new BoxVolumeStub();
        stub(factory.getVolume(any(), any())).toReturn(boxVolume1);
    }

    @Test
    public void returnsSameFactoryForSameAccountAndIdentity() throws Exception {
        BoxVolumeFactory sut = new CachedBoxVolumeFactory(factory);
        assertSame(boxVolume1, sut.getVolume(account, identity));

        assertSame(boxVolume1, sut.getVolume(account, identity));
        verify(factory, times(1)).getVolume(any(), any());
    }

    @Test
    public void retunsNewFactoryForAnotherAccount() throws Exception {
        BoxVolumeFactory sut = new CachedBoxVolumeFactory(factory);
        sut.getVolume(account, identity);

        stub(factory.getVolume(any(), any())).toReturn(new BoxVolumeStub());
        assertNotSame(boxVolume1, sut.getVolume(account2, identity));
    }

    @Test
    public void returnsNewFactoryForAnotherIdentity() throws Exception {
        BoxVolumeFactory sut = new CachedBoxVolumeFactory(factory);
        sut.getVolume(account, identity);

        stub(factory.getVolume(any(), any())).toReturn(new BoxVolumeStub());
        assertNotSame(boxVolume1, sut.getVolume(account, identity2));
    }
}
