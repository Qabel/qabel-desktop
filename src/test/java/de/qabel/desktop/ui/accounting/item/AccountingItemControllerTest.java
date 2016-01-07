package de.qabel.desktop.ui.accounting.item;

import de.qabel.core.config.Account;
import de.qabel.core.config.Identity;
import de.qabel.desktop.ui.AbstractControllerTest;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;

public class AccountingItemControllerTest extends AbstractControllerTest {
	@Test
	public void identityLabelsAreFilledCorrectly() throws Exception {
		diContainer.put("account", new Account("providerName", "userName", "authString"));
		Identity identity = new Identity("my identity", null, null);

		Map<String, Identity> params = new HashMap<String, Identity>();
		params.put("identity", identity);
		AccountingItemView view = new AccountingItemView(params::get);
		view.getView();
		AccountingItemController controller = (AccountingItemController)view.getPresenter();

		assertEquals("my identity", controller.alias.getText());
		assertEquals("providerName", controller.provider.getText());
		assertEquals("userName", controller.mail.getText());
	}
}
