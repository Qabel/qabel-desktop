package de.qabel.desktop.ui.accounting.item;

import com.airhacks.afterburner.views.FXMLView;
import de.qabel.core.config.Identity;
import de.qabel.core.config.factory.DropUrlGenerator;
import de.qabel.core.config.factory.IdentityBuilder;
import de.qabel.desktop.ui.AbstractGuiTest;
import org.junit.Before;
import org.junit.Test;

import java.net.URISyntaxException;

import static org.junit.Assert.assertEquals;
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
        page.editAlias();
        assertEquals("new alias identity", controller.getIdentity().getAlias());
    }

    @Test
    public void selectsIdentity() {
        page.select();
        waitUntil(() -> identity.equals(clientConfiguration.getSelectedIdentity()));
        assertTrue(controller.selectedRadio.isSelected());
    }
}
