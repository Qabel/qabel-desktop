package de.qabel.desktop.ui.sync.setup;

import com.tngtech.java.junit.dataprovider.DataProvider;
import com.tngtech.java.junit.dataprovider.DataProviderRunner;
import com.tngtech.java.junit.dataprovider.UseDataProvider;
import de.qabel.core.config.Account;
import de.qabel.core.config.Identity;
import de.qabel.desktop.config.BoxSyncConfig;
import de.qabel.desktop.config.DefaultBoxSyncConfig;
import de.qabel.desktop.daemon.sync.worker.index.SyncIndexFactory;
import de.qabel.desktop.daemon.sync.worker.index.memory.InMemorySyncIndexFactory;
import de.qabel.desktop.nio.boxfs.BoxFileSystem;
import de.qabel.desktop.nio.boxfs.BoxPath;
import de.qabel.desktop.repository.exception.PersistenceException;
import de.qabel.desktop.ui.AbstractControllerTest;
import javafx.scene.Node;
import javafx.stage.Stage;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.nio.file.Path;
import java.nio.file.Paths;

import static de.qabel.desktop.AsyncUtils.waitUntil;
import static org.junit.Assert.*;

@RunWith(DataProviderRunner.class)
public class SyncSetupControllerTest extends AbstractControllerTest {

    private SyncSetupController controller;
    private Identity identity;
    private SyncIndexFactory indexFactory = new InMemorySyncIndexFactory();

    @Override
    public void setUp() throws Exception {
        super.setUp();
        identity = identityBuilderFactory.factory().withAlias("alias").build();
        clientConfiguration.selectIdentity(identity);
        SyncSetupView view = new SyncSetupView();
        Node node = view.getView();
        controller = (SyncSetupController) view.getPresenter();

        controller.setName("valid");
        controller.setLocalPath(Paths.get("path").toAbsolutePath().toString());
        controller.setRemotePath(Paths.get("path").toAbsolutePath().toString());
    }

    @Test
    public void testDetectsEmptyName() {
        controller.setName("");
        assertFalse("empty name did not invalidate form", controller.isValid());
        assertErrorClass(controller.name);
    }

    private void assertErrorClass(Node name) {
        assertTrue("field has no error class", name.getStyleClass().contains("error"));
    }

    private void assertNoErrorClass(Node name) {
        assertFalse("field has error class", name.getStyleClass().contains("error"));
    }

    @Test
    public void testValidatesOnValidProperties() {
        assertTrue("empty remotePath did not invalidate form", controller.isValid());
    }

    @Test
    public void testDetectsEmptyLocalPath() {
        controller.setLocalPath("");
        assertFalse("empty localPath did not invalidate form", controller.isValid());
        assertErrorClass(controller.localPath);
    }

    @Test
    public void testDetectsEmptyRemotePath() {
        controller.setRemotePath("");
        assertFalse("empty remotePath did not invalidate form", controller.isValid());
        assertErrorClass(controller.remotePath);
    }

    @Test
    public void fillsIdentityWithAlias() {
        assertEquals("alias", controller.identity.getText());
    }

    @Test
    public void createsSyncConfigOnSubmit() throws Exception {
        Account account = new Account("a", "b", "c");
        clientConfiguration.setAccount(account);
        final Stage[] stage = new Stage[1];

        runLaterAndWait(() -> stage[0] = new Stage());
        runLaterAndWait(stage[0]::show);
        controller.setStage(stage[0]);

        controller.setName("Sync name");
        String localPath = Paths.get("tmp").toAbsolutePath().toString();
        controller.setLocalPath(localPath);
        controller.setRemotePath("/");
        controller.createSyncConfigFromForm();

        assertEquals(1, boxSyncRepository.findAll().size());
        BoxSyncConfig config = boxSyncRepository.findAll().get(0);
        assertEquals("Sync name", config.getName());
        assertEquals(localPath, config.getLocalPath().toString());
        assertEquals("/", config.getRemotePath().toString());
        assertSame(identity, config.getIdentity());
        assertSame(account, config.getAccount());
        waitUntil(() -> !stage[0].isShowing());
    }

    private static Path local(String path) {
        return Paths.get(path);
    }

    private static BoxPath remote(String path) {
        return BoxFileSystem.get(path);
    }

    @DataProvider
    public static Object[][] invalidSyncPaths() {
        return new Object[][]{
            {local("/a"),       remote("/b"),       local("/a/child"),  remote("/b")},
            {local("/a/child"), remote("/b"),       local("/a"),        remote("/b")},
            {local("/a"),       remote("/b"),       local("/a"),        remote("/b/child")},
            {local("/a"),       remote("/b/child"), local("/a"),        remote("/b")}
        };
    }

    @Test(expected = ConflictingSyncPathsException.class)
    @UseDataProvider("invalidSyncPaths")
    public void deniesInvalidSyncConfigsToPreventSyncLoops(Path lPath1, BoxPath rPath1, Path lPath2, BoxPath rPath2) throws Exception {
        BoxSyncConfig config1 = new DefaultBoxSyncConfig("config1", lPath1, rPath1, identity, account, indexFactory);
        boxSyncRepository.save(config1);

        controller.setLocalPath(lPath2.toString());
        controller.setRemotePath(rPath2.toString());
        assertFalse("intersectingPath did not trigger error", controller.isValid());
        assertErrorClass(controller.localPath);
        controller.createSyncConfig(account, lPath2, rPath2);
    }

    @DataProvider
    public static Object[][] validSyncPaths() {
        return new Object[][]{
            {local("/home/user/rootSync"), remote("/"), local("/home/user/specialFolder"), remote("/specialFolder")}
        };
    }

    @Test
    @UseDataProvider("validSyncPaths")
    public void allowsValidSyncPathCombinations(Path lPath1, BoxPath rPath1, Path lPath2, BoxPath rPath2) throws Exception {
        BoxSyncConfig config1 = new DefaultBoxSyncConfig("config1", lPath1, rPath1, identity, account, indexFactory);
        boxSyncRepository.save(config1);


        controller.setLocalPath(lPath2.toString());
        controller.setRemotePath(rPath2.toString());
        assertTrue("not intersecting paths did trigger error", controller.isValid());
        assertNoErrorClass(controller.localPath);

        controller.createSyncConfig(account, lPath2, rPath2);
    }

    @Test
    public void startButtonIsEnabledWhenValidationFails() {
        assertFalse(controller.start.isDisabled());
    }

    @Test
    public void startButtonIsDisabledWhenValidationFails() {
        controller.setName("");
        assertTrue(controller.start.isDisabled());
    }
}
