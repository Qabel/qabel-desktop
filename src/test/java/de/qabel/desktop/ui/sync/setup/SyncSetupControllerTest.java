package de.qabel.desktop.ui.sync.setup;

import de.qabel.core.config.Account;
import de.qabel.core.config.Identity;
import de.qabel.desktop.config.BoxSyncConfig;
import de.qabel.desktop.ui.AbstractControllerTest;
import javafx.scene.Node;
import javafx.stage.Stage;
import org.junit.Test;

import java.nio.file.Paths;

import static de.qabel.desktop.AsyncUtils.waitUntil;
import static org.junit.Assert.*;

public class SyncSetupControllerTest extends AbstractControllerTest {

    private SyncSetupController controller;
    private Identity identity;

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
        controller.createSyncConfig();

        assertEquals(1, boxSyncConfigRepository.findAll().size());
        BoxSyncConfig config = boxSyncConfigRepository.findAll().get(0);
        assertEquals("Sync name", config.getName());
        assertEquals(localPath, config.getLocalPath().toString());
        assertEquals("/", config.getRemotePath().toString());
        assertSame(identity, config.getIdentity());
        assertSame(account, config.getAccount());
        waitUntil(() -> !stage[0].isShowing());
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
