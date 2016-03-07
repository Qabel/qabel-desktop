package de.qabel.desktop.ui.contact;

import com.airhacks.afterburner.views.FXMLView;
import de.qabel.core.config.Contact;
import de.qabel.core.config.Identity;
import de.qabel.desktop.ui.AbstractGuiTest;
import org.junit.Test;

import static junit.framework.TestCase.assertEquals;

public class ContactGuiTest extends AbstractGuiTest<ContactController> {

	@Override
	protected FXMLView getView() {
		return new ContactView();
	}

	@Test
	public void testDeleteContact() throws Exception {
		Identity identity = identityBuilderFactory.factory().withAlias("TestAlias").build();
		Contact c = new Contact("TestContact", identity.getDropUrls(), identity.getEcPublicKey());

		runLaterAndWait(() -> clientConfiguration.selectIdentity(identity));

		contactRepository.save(c, identity);
		runLaterAndWait(() -> controller.loadContacts());
		clickOn("#delete");

		assertEquals(0, controller.contactsFromRepo.getContacts().size());
	}

	@Test
	public void testDummyContact() throws Exception {
		assertEquals(1, controller.contactList.getChildren().size());
	}

}
