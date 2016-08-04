package de.qabel.desktop.ui.sync.item;

import com.airhacks.afterburner.views.FXMLView;
import de.qabel.core.config.Account;
import de.qabel.core.config.Identity;
import de.qabel.core.config.factory.DropUrlGenerator;
import de.qabel.core.config.factory.IdentityBuilderFactory;
import de.qabel.desktop.config.BoxSyncConfig;
import de.qabel.desktop.config.DefaultBoxSyncConfig;
import de.qabel.desktop.daemon.sync.worker.FakeSyncer;
import de.qabel.desktop.daemon.sync.worker.index.memory.InMemorySyncIndexFactory;
import de.qabel.desktop.nio.boxfs.BoxFileSystem;
import de.qabel.desktop.ui.AbstractGuiTest;
import org.junit.Before;
import org.junit.Test;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.junit.Assert.*;

public class SyncItemControllerGuiTest extends AbstractGuiTest<SyncItemController> {
    private Identity identity;
    private Account account;
    BoxSyncConfig syncConfig;
    private FakeSyncer syncer;
    SyncItemPage page;
    private Path local;

    @Override
    @Before
    public void setUp() throws Exception {
        identityBuilderFactory = new IdentityBuilderFactory(new DropUrlGenerator("http://localhost:5000"));
        identity = identityBuilderFactory.factory().withAlias("Bobby").build();
        account = new Account("a", "b", "c");
        local = Files.createTempDirectory(Paths.get("/tmp"), "testsync").toAbsolutePath();
        syncConfig = new DefaultBoxSyncConfig("testsync", local, BoxFileSystem.get("/tmp"), identity, account, new InMemorySyncIndexFactory());
        syncer = new FakeSyncer(syncConfig);
        syncConfig.setSyncer(syncer);
        super.setUp();
        boxSyncRepository.save(syncConfig);
        page = new SyncItemPage(baseFXRobot, robot, controller);
    }

    @Override
    protected FXMLView getView() {
        return new SyncItemView(s -> s.equals("syncConfig") ? syncConfig : null);
    }

    @Test
    public void showsItemsProperties() {
        assertEquals("testsync", page.name());
        assertEquals(local.toString(), page.localPath());
        assertEquals("/tmp", page.remotePath());

    }

    @Test
    public void avatarLabelCheck() throws Exception {
        assertEquals("B", page.avatar());
    }

    @Test
    public void refreshesOnConfigChange() {
        runLaterAndWait(() -> {
            syncConfig.setName("changed");
            syncConfig.setLocalPath(Paths.get("/tmp/to something"));
            syncConfig.setRemotePath(BoxFileSystem.get("else"));
        });

        assertEquals("changed", page.name());
        assertEquals(Paths.get("/tmp/to something").toAbsolutePath().toString(), page.localPath());
        assertEquals("/else", page.remotePath());
    }

    @Test
    public void deletesSyncCleanly() throws Exception {
        page.delete().yes();
        waitUntil(syncer::isStopped);
        assertTrue(boxSyncRepository.findAll().isEmpty());
    }

    @Test
    public void doesNotDeleteSyncIfUserIsUnsure() throws Exception {
        page.delete().cancel();
        waitUntil(() -> controller.confirmationDialog == null);
        assertFalse(syncer.isStopped());
        assertFalse(boxSyncRepository.findAll().isEmpty());
    }
}
