package de.qabel.desktop.ui.accounting.identitycontextmenu;

import com.airhacks.afterburner.views.FXMLView;
import de.qabel.core.config.Identity;
import de.qabel.core.config.factory.DropUrlGenerator;
import de.qabel.core.config.factory.IdentityBuilder;
import de.qabel.desktop.ui.AbstractGuiTest;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import java.net.URISyntaxException;

import static org.junit.Assert.assertTrue;


public class IdentityContextMenuGuiTest extends AbstractGuiTest<IdentityContextMenuController> {
    private Identity identity;
    private IdentityContextMenuPage page;

    @Before
    @Override
    public void setUp() throws Exception {
        super.setUp();
        page = new IdentityContextMenuPage(baseFXRobot, robot, controller);
//        controller.identityContextMenu.setVisible(true);
//        controller.layoutWindow = new Pane();
    }

    @Override
    protected FXMLView getView() {
        try {
            identity = new IdentityBuilder(new DropUrlGenerator("http://localhost")).withAlias("alias").build();
        } catch (URISyntaxException e) {
            throw new IllegalStateException("won't happen", e);
        }
        return new IdentityContextMenuView(generateInjection("identity", identity));
    }

    @Test
    public void canShowIdentityEdit() {
        page.openIdentityEdit();
        runLaterAndWait(this::assertEditIsShowing);
        robot.sleep(4000);
    }

    private void assertEditIsShowing() {
        assertTrue(controller.identityEditController.isShowing());
    }

    @Ignore
    @Test
    public void canEditIdentity() {
        page.openIdentityEdit();
        page.changeIdentity("someNewAlias", "someNewMail@mail.com", "PHONE-010101");
    }
}
