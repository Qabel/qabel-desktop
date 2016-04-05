package de.qabel.desktop.ui.accounting.item;

import com.airhacks.afterburner.views.FXMLView;
import de.qabel.core.config.Identity;
import de.qabel.desktop.ui.AbstractGuiTest;
import javafx.scene.control.ButtonType;
import org.junit.Before;
import org.junit.Test;

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
		identity = new Identity("alias", null, null);
		return new AccountingItemView(generateInjection("identity", identity));
	}

	@Test
	public void testEdit() throws Exception {
        page.edit().inputAndConfirm("new alias");
		waitUntil(() -> identity.getAlias().equals("new alias"));
	}

	@Test
	public void selectsIdentity() {
        page.select();
		waitUntil(() -> identity.equals(clientConfiguration.getSelectedIdentity()));
		assertTrue(controller.selectedRadio.isSelected());
	}
}
