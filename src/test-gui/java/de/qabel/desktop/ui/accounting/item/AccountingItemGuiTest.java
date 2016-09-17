package de.qabel.desktop.ui.accounting.item;

import com.airhacks.afterburner.views.FXMLView;
import de.qabel.core.config.Identity;
import de.qabel.core.config.factory.DropUrlGenerator;
import de.qabel.core.config.factory.IdentityBuilder;
import de.qabel.desktop.ui.AbstractGuiTest;
import de.qabel.desktop.ui.accounting.identitycontextmenu.IdentityContextMenuPage;
import org.junit.Before;
import org.junit.Test;

import java.net.URISyntaxException;

import static org.junit.Assert.assertTrue;

public class AccountingItemGuiTest extends AbstractGuiTest<AccountingItemController> {

    private Identity identity;
    private AccountingItemPage page;

    @Before
    @Override
    public void setUp() throws Exception {
        super.setUp();
        controller.layoutWindow = controller.root;
        page = new AccountingItemPage(baseFXRobot, robot, controller);
    }

    @Override
    protected FXMLView getView() {
        try {
            identity = new IdentityBuilder(new DropUrlGenerator("http://localhost")).withAlias("alias").build();
        } catch (URISyntaxException e) {
            throw new IllegalStateException("won't happen", e);
        }
        return new AccountingItemView(generateInjection("identity", identity));
    }

    @Test
    public void selectsIdentity() {
        page.select();
        waitUntil(() -> identity.equals(clientConfiguration.getSelectedIdentity()));
        assertTrue(controller.selectedRadio.isSelected());
    }

    @Test
    public void openContextMenu() {
        page.clickOnContextMenuIcon();
        assertTrue(controller.contextMenuController.isVisible());
    }

    @Test
    public void openQrCode() {
        page.clickOnContextMenuIcon();

        IdentityContextMenuPage contextPage = new IdentityContextMenuPage(baseFXRobot, robot, controller.contextMenuController);
        contextPage.openQrCode();
//        runLaterAndWait(() -> assertTrue(contextPage.qrcodeIsVisible()));
    }

}
