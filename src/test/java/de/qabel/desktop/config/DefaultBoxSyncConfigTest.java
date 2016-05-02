package de.qabel.desktop.config;

import de.qabel.core.config.Account;
import de.qabel.core.config.Identity;
import de.qabel.desktop.daemon.sync.worker.index.memory.InMemorySyncIndexFactory;
import org.junit.Before;
import org.junit.Test;

import java.nio.file.Path;
import java.nio.file.Paths;

import static org.junit.Assert.*;

public class DefaultBoxSyncConfigTest {
    private Identity identity = new Identity("alias", null, null);
    private Account account = new Account("a", "b", "c");
    private DefaultBoxSyncConfig config;
    private boolean[] updated = new boolean[]{false};
    private Path localPath;
    private Path remotePath;

    @Before
    public void setUp() {
        localPath = Paths.get("wayne").toAbsolutePath();
        remotePath = Paths.get("train");
        config = new DefaultBoxSyncConfig(localPath, remotePath, identity, account, new InMemorySyncIndexFactory());

        config.addObserver((o, arg) -> updated[0] = true);
    }

    private void assertNotUpdated() {
        assertFalse("observer was notified even though it should not", wasUpdated());
    }

    public void assertUpdated() {
        assertTrue("observer was not notified", wasUpdated());
    }

    private boolean wasUpdated() {
        return updated[0];
    }

    @Test
    public void doesNotNotifyIfLocalPathWasntChanged() {
        config.setLocalPath(config.getLocalPath());
        assertNotUpdated();
    }

    @Test
    public void notifiesOnLocalPathChange() {
        config.setLocalPath(Paths.get("dwain").toAbsolutePath());
        assertUpdated();
        assertEquals("dwain", config.getLocalPath().getFileName().toString());
    }

    @Test
    public void doesNotNotifyIfRemotePathWasntChanged() {
        config.setRemotePath(config.getRemotePath());
        assertNotUpdated();
    }

    @Test
    public void notifiesOnRemotePathChange() {
        config.setRemotePath(Paths.get("western"));
        assertUpdated();
        assertEquals("western", config.getRemotePath().getFileName().toString());
    }

    @Test
    public void doesNotNotifyIfPauseStatusWasChanged() {
        config.pause();

        resetObserver();
        config.pause();
        assertNotUpdated();
    }

    @Test
    public void notifiesOnPause() {
        config.pause();
        assertUpdated();
        assertTrue(config.isPaused());
    }

    @Test
    public void notifiesOnUnpause() {
        config.pause();
        resetObserver();

        config.unpause();
        assertUpdated();
        assertFalse(config.isPaused());
    }

    @Test
    public void showsItsIdentits() {
        assertSame(identity, config.getIdentity());
    }

    @Test
    public void showsItsAccount() {
        assertSame(account, config.getAccount());
    }

    @Test(expected = IllegalArgumentException.class)
    public void preventsHardToDebugRelativeLocalPath() {
        config.setLocalPath(Paths.get("a"));
    }
    @Test(expected = IllegalArgumentException.class)
    public void preventsHardToDebugRelativeLocalPathOnConstructor() {
        config = new DefaultBoxSyncConfig(Paths.get("a"), remotePath, identity, account, new InMemorySyncIndexFactory());
    }

    private void resetObserver() {
        updated[0] = false;
    }
}
