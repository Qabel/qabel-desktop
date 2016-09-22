package de.qabel.desktop.ui.accounting.item;

import com.airhacks.afterburner.views.FXMLView;
import de.qabel.desktop.ui.AbstractGuiTest;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertTrue;

public class AccountingItemGuiTest extends AbstractGuiTest<AccountingItemController> {
    private AccountingItemPage page;

    @Before
    @Override
    public void setUp() throws Exception {
        super.setUp();
        page = new AccountingItemPage(baseFXRobot, robot, controller);
    }

    @Override
    protected FXMLView getView() {
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
        page.openContextMenu();
    }

    @Test
    public void openQrCode() {
        page.openContextMenu()
            .openQrCode();
    }
}
