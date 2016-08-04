package de.qabel.desktop.ui.sync.item;

import com.airhacks.afterburner.views.FXMLView;
import de.qabel.core.config.Account;
import de.qabel.core.config.Identity;
import de.qabel.core.config.factory.DropUrlGenerator;
import de.qabel.core.config.factory.IdentityBuilderFactory;
import de.qabel.desktop.config.DefaultBoxSyncConfig;
import de.qabel.desktop.daemon.sync.worker.index.memory.InMemorySyncIndexFactory;
import de.qabel.desktop.nio.boxfs.BoxFileSystem;
import de.qabel.desktop.ui.AbstractControllerTest;
import de.qabel.desktop.ui.accounting.avatar.AvatarController;
import javafx.scene.layout.Pane;
import org.junit.Assert;
import org.junit.Test;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class SyncItemControllerTest extends AbstractControllerTest {

    private DefaultBoxSyncConfig syncConfig;
    private Identity identity;

    @Override
    public void setUp() throws Exception {
        super.setUp();
        IdentityBuilderFactory identityBuilderFactory = new IdentityBuilderFactory(new DropUrlGenerator("http://localhost:5000"));
        Account account = new Account("a", "b", "c");
        Path local = Files.createTempDirectory(Paths.get("/tmp"), "testsync").toAbsolutePath();

        identity = identityBuilderFactory.factory().withAlias("Bobby").build();
        syncConfig = new DefaultBoxSyncConfig("testsync", local, BoxFileSystem.get("/tmp"), identity, account, new InMemorySyncIndexFactory());

    }

    @Test
    public void createAndShowsAvatar() throws Exception {
        SyncItemController controller = createController();

        FXMLView avatarView = controller.createAvatar(identity.getAlias(), getContainer());
        AvatarController avatarController = (AvatarController) avatarView.getPresenter();

        Assert.assertEquals("B", avatarController.getAvatar().getText());
    }

    private Pane getContainer() {
        Pane container = new Pane();
        container.setId("someFooContainer");
        return container;
    }

    private SyncItemController createController() {
        SyncItemView view = new SyncItemView(s -> s.equals("syncConfig") ? syncConfig : null);
        return (SyncItemController) view.getPresenter();
    }
}
