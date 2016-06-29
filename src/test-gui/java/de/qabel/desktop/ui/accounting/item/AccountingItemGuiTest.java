package de.qabel.desktop.ui.accounting.item;

import com.airhacks.afterburner.views.FXMLView;
import de.qabel.core.config.Identity;
import de.qabel.desktop.config.factory.DropUrlGenerator;
import de.qabel.desktop.config.factory.IdentityBuilder;
import de.qabel.desktop.ui.AbstractGuiTest;
import javafx.scene.control.ButtonType;
import org.junit.Before;
import org.junit.Test;

import java.net.URISyntaxException;

import static de.qabel.desktop.AsyncUtils.waitUntil;
import static org.junit.Assert.assertTrue;

public class AccountingItemGuiTest extends AbstractGuiTest<AccountingItemController> {

    private Identity identity;
    private AccountingItemPage page;

    @Before
    @Override
    public void setUp() throws Exception {
        super.setUp();
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
    public void testEdit() throws Exception {
        page.openMenu();
    }

    @Test
    public void selectsIdentity() {
        page.select();
        waitUntil(() -> identity.equals(clientConfiguration.getSelectedIdentity()));
        assertTrue(controller.selectedRadio.isSelected());
    }
}
