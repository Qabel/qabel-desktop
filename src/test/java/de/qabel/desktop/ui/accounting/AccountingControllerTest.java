package de.qabel.desktop.ui.accounting;

import de.qabel.core.config.Identity;
import de.qabel.desktop.repository.exception.PersistenceException;
import de.qabel.desktop.ui.accounting.item.AccountingItemController;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.assertEquals;


public class AccountingControllerTest extends de.qabel.desktop.ui.AbstractControllerTest {

	@Test
	public void showsIdentities() throws PersistenceException {
		Identity identity = new Identity("alias", null, null);
		identityRepository.save(identity);

		AccountingController controller = getAccountingController();

		assertEquals(1, controller.identityList.getChildren().size());
		assertEquals(1, controller.itemViews.size());

		assertEquals(identity, ((AccountingItemController)controller.itemViews.get(0).getPresenter()).getIdentity());
	}

	private AccountingController getAccountingController() {
		AccountingView view = new AccountingView();
		view.getView();
		return (AccountingController) view.getPresenter();
	}

	@Test
	public void addsIdentitiesWithAlias() throws Exception {
		AccountingController controller = getAccountingController();
		controller.addIdentityWithAlias("my ident");

		List<Identity> identities = identityRepository.findAll();
		assertEquals(1, identities.size());
		assertEquals("my ident", identities.get(0).getAlias());
	}
}