package de.qabel.desktop.ui.invite;

import de.qabel.desktop.ui.AbstractControllerTest;
import org.junit.Test;

import java.util.Locale;

import static org.junit.Assert.assertEquals;


public class InviteControllerTest extends AbstractControllerTest {
	InviteController controller;

	@Test
	public void createEMailBodyTest() {
		createController();
		String body = controller.createEMailBody();
		assertEquals("TEST%20TEST,%0D%0ATEST%20TEST", body);
	}

	@Test
	public void createEMailSubjectTest() {
		createController();
		String subject = controller.createEMailSubject();
		assertEquals("Invation%20to%20Qabel", subject);

	}

	private void createController() {
		Locale.setDefault(new Locale("te", "ST"));
		InviteView view = new InviteView();
		controller = (InviteController) view.getPresenter();
	}

}
