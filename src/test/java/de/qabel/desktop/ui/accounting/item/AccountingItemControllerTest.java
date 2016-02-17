package de.qabel.desktop.ui.accounting.item;

import de.qabel.core.config.Account;
import de.qabel.core.config.Identities;
import de.qabel.core.config.Identity;
import de.qabel.core.crypto.QblECKeyPair;
import de.qabel.desktop.config.ClientConfiguration;
import de.qabel.desktop.ui.AbstractControllerTest;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;

public class AccountingItemControllerTest extends AbstractControllerTest {
	@Test
	public void identityLabelsAreFilledCorrectly() throws Exception {
		Account account = new Account("providerName", "userName", "authString");
		((ClientConfiguration) diContainer.get("clientConfiguration")).setAccount(account);
		Identity identity = new Identity("my identity", null, null);
		AccountingItemController controller = getController(identity);


		assertEquals("my identity", controller.alias.getText());
		assertEquals("userName", controller.mail.getText());
	}

	private AccountingItemController getController(Identity identity) {
		AccountingItemView view = new AccountingItemView(createParams(identity)::get);
		view.getView();
		return (AccountingItemController) view.getPresenter();
	}

	private Map<String, Identity> createParams(Identity identity) {
		Map<String, Identity> params = new HashMap<>();
		params.put("identity", identity);
		return params;
	}

	@Test
	public void savesAlias() throws Exception {
		Identity i = new Identity("alias", null, new QblECKeyPair());
		AccountingItemController controller = getController(i);
		controller.setAlias("new alias");
		assertEquals("new alias", controller.alias.getText());
		Identities results = identityRepository.findAll();
		Identity identity = results.getByKeyIdentifier(i.getKeyIdentifier());
		assertEquals("new alias", identity.getAlias());
	}
}
