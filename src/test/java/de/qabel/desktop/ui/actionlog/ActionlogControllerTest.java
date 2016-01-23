package de.qabel.desktop.ui.actionlog;

import de.qabel.core.config.Account;
import de.qabel.core.config.Identity;
import de.qabel.core.crypto.QblECKeyPair;
import de.qabel.desktop.ui.AbstractControllerTest;
import org.junit.Test;

import static org.junit.Assert.assertNotNull;


public class ActionlogControllerTest extends AbstractControllerTest {

	ActionlogController controller;

	@Test
	public void injectTest() {
		ActionlogView view = new ActionlogView();
		Identity i = new Identity("test", null, new QblECKeyPair());
		clientConfiguration.selectIdentity(i);
		clientConfiguration.setAccount(new Account("Provider", "user", "auth"));
		controller = (ActionlogController) view.getPresenter();
		assertNotNull(controller);
	}
}