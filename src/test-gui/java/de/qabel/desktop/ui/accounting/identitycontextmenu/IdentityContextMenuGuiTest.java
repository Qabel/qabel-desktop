package de.qabel.desktop.ui.accounting.identitycontextmenu;

import com.airhacks.afterburner.views.FXMLView;
import de.qabel.core.config.Identity;
import de.qabel.core.config.factory.DropUrlGenerator;
import de.qabel.core.config.factory.IdentityBuilder;
import de.qabel.desktop.ui.AbstractGuiTest;
import org.junit.Before;
import org.junit.Test;

import java.net.URISyntaxException;

import static de.qabel.desktop.AsyncUtils.assertAsync;
import static junit.framework.TestCase.assertFalse;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;


public class IdentityContextMenuGuiTest extends AbstractGuiTest<IdentityContextMenuController> {
    private Identity identity;
    private IdentityContextMenuPage page;

    private String alias = "myNewAlias";
    private String email = "myNewMail@mail.com";
    private String phone = "1337/12312312";

    @Before
    @Override
    public void setUp() throws Exception {
        super.setUp();
        page = new IdentityContextMenuPage(baseFXRobot, robot, controller);
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
    public void canOpenAndCloseMenu() {
        waitUntil(() -> controller.popOver != null);

        controller.openMenu();
        assertAsync(() -> assertTrue(controller.popOver.isShowing()));

        controller.closeMenu();
        assertAsync(() -> assertFalse(controller.popOver.isShowing()));
    }

    @Test
    public void canShowIdentityEdit() {
        openIdentityEdit();

        assertAsync(() -> assertTrue(controller.identityEditController.isShowing()));
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

    private void openIdentityEdit() {
        controller.openMenu();
        page.openIdentityEdit();
        waitUntil(() -> controller.identityEditController != null);
    }
}
