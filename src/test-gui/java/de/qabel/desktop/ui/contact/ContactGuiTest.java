package de.qabel.desktop.ui.contact;

import com.airhacks.afterburner.views.FXMLView;
import de.qabel.core.config.Contact;
import de.qabel.core.config.Identity;
import de.qabel.core.crypto.QblECKeyPair;
import de.qabel.desktop.repository.exception.EntityNotFoundExcepion;
import de.qabel.desktop.repository.exception.PersistenceException;
import de.qabel.desktop.ui.AbstractGuiTest;
import org.junit.Test;

import static junit.framework.TestCase.assertEquals;

public class ContactGuiTest extends AbstractGuiTest<ContactController> {

	@Override
	protected FXMLView getView() {
		return new ContactView();
	}

	@Test
	public void testDeleteContact() throws EntityNotFoundExcepion {
		Identity identity = new Identity("alias", null, new QblECKeyPair());

		Contact c = new Contact("TestContact", null, identity.getEcPublicKey());
		runLaterAndWait(() -> {
			try {
				contactRepository.save(c, null);
				controller.loadContacts();
			} catch (EntityNotFoundExcepion | PersistenceException entityNotFoundExcepion) {
				entityNotFoundExcepion.printStackTrace();
			}
		});
		clickOn("#delete");
		assertEquals(1, controller.contactList.getChildren().size());
		assertEquals(0, controller.contactsFromRepo.getContacts().size());
	}
}
