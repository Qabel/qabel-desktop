package de.qabel.desktop.ui.accounting.identitycontextmenu;

import com.airhacks.afterburner.views.FXMLView;
import de.qabel.core.config.Identity;
import de.qabel.core.config.factory.DropUrlGenerator;
import de.qabel.core.config.factory.IdentityBuilder;
import de.qabel.desktop.ui.AbstractGuiTest;
import org.junit.After;
import org.junit.Test;

import java.net.URISyntaxException;

import static junit.framework.TestCase.assertEquals;
import static junit.framework.TestCase.assertFalse;
import static org.junit.Assert.assertTrue;


public class IdentityContextMenuGuiTest extends AbstractGuiTest<IdentityContextMenuController> {

    private Identity identity;
    private IdentityContextMenuPage page;

    private String alias = "myNewAlias";
    private String email = "myNewMail@mail.com";
    private String phone = "1337/12312312";

    @Override
    public void setUp() throws Exception {
        super.setUp();
        page = new IdentityContextMenuPage(baseFXRobot, robot, controller);
        controller = getController();
    }

    @Override
    @After
    public void tearDown() throws Exception {
        super.tearDown();
        closeMenu();
        controller = null;
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


    private void openMenu() {
        controller.openMenu();
        waitUntil(() -> controller.popOver != null);
        waitUntil(() -> controller.popOver.isShowing());
    }

    @Test
    public void canOpenMenu() {
        openMenu();
        assertTrue(controller.popOver.isShowing());
    }

    @Test
    public void canCloseMenu() {
        openMenu();
        closeMenu();
        assertFalse(controller.popOver.isShowing());
    }

    private void closeMenu() {
        controller.closeMenu();
        waitUntil(() -> controller.popOver.isShowing() == false);
    }

    @Test
    public void openQr() {
        openQrCode();
        runLaterAndWait(() -> assertTrue(controller.qrcodeController.isVisible()));
        closeMenu();
    }

    private void openQrCode() {
        fakeLayoutWindow();
        openMenu();
        page.openQrCode();
        waitUntil(() -> controller.qrcodeController != null);
    }

    private void fakeLayoutWindow() {
        controller.layoutWindow = controller.identityContextMenu;
    }

    @Test
    public void canShowIdentityEdit() {
        openIdentityEdit();
        runLaterAndWait(() -> assertTrue(controller.identityEditController.isShowing()));
    }

    private void openIdentityEdit() {
        fakeLayoutWindow();
        openMenu();
        page.openIdentityEdit();
        waitUntil(() -> controller.identityEditController != null);
    }

    @Test
    public void canEditIdentity() {
        openIdentityEdit();

        page.changeIdentity(alias, email, phone);
        waitUntil(() -> identity.getAlias().equals(alias));

        assertEquals(alias, identity.getAlias());
        assertEquals(email, identity.getEmail());
        assertEquals(phone, identity.getPhone());
    }


}
