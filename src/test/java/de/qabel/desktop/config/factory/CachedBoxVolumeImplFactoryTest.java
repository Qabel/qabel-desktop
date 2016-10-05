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
    private BoxVolumeFactory sut;


    @Before
    public void setUp() throws Exception {
        account = new Account("a", "b", "c");
        account2 = new Account("a2", "b2", "c2");
        identity = new IdentityBuilderFactory(new DropUrlGenerator("http://localhost")).factory().withAlias("foo").build();
        identity2 = new IdentityBuilderFactory(new DropUrlGenerator("http://localhost")).factory().withAlias("foo").build();
        factory = mock(BoxVolumeFactory.class);
        boxVolume1 = new BoxVolumeStub();
        when(factory.getVolume(any(), any())).thenReturn(boxVolume1).thenReturn(new BoxVolumeStub());
        sut = new CachedBoxVolumeFactory(factory);
    }

    @Test
    public void returnsSameFactoryForSameAccountAndIdentity() throws Exception {
        assertSame(boxVolume1, sut.getVolume(account, identity));

        assertSame(boxVolume1, sut.getVolume(account, identity));
        verify(factory, times(1)).getVolume(any(), any());
    }

    @Test
    public void retunsNewFactoryForAnotherAccount() throws Exception {
        sut.getVolume(account, identity);

        assertReturnsAnotherInstance(account2, identity);
    }

    private void assertReturnsAnotherInstance(Account nextAccount,Identity nextIdentity) {
        assertNotSame(boxVolume1, sut.getVolume(nextAccount, nextIdentity));
        verify(factory, times(1)).getVolume(account, identity);
        verify(factory, times(1)).getVolume(nextAccount, nextIdentity);
        verifyNoMoreInteractions(factory);
    }

    @Test
    public void returnsNewFactoryForAnotherIdentity() throws Exception {
        sut.getVolume(account, identity);

        assertReturnsAnotherInstance(account, identity2);
    }
}
