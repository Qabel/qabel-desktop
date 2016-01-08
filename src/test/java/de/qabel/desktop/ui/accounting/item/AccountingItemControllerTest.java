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
		AccountingItemController controller = getController(identity);

		assertEquals("my identity", controller.alias.getText());
		assertEquals("providerName", controller.provider.getText());
		assertEquals("userName", controller.mail.getText());
	}

	private AccountingItemController getController(Identity identity) {
		AccountingItemView view = new AccountingItemView(createParams(identity)::get);
		view.getView();
		return (AccountingItemController)view.getPresenter();
	}

	private Map<String, Identity> createParams(Identity identity) {
		Map<String, Identity> params = new HashMap<>();
		params.put("identity", identity);
		return params;
	}

	@Test
	public void savesAlias() throws Exception {
		AccountingItemController controller = getController(new Identity("alias", null, null));
		controller.setAlias("new alias");
		assertEquals("new alias", controller.alias.getText());

		assertEquals("new alias", identityRepository.findAll().get(0).getAlias());
	}
}
