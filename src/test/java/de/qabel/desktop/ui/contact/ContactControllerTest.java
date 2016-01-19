package de.qabel.desktop.ui.contact;

import de.qabel.core.config.Account;
import de.qabel.core.config.Contact;
import de.qabel.core.config.Identity;
import de.qabel.core.crypto.QblECKeyPair;
import de.qabel.core.exceptions.QblDropInvalidURL;
import de.qabel.desktop.repository.exception.EntityNotFoundExcepion;
import de.qabel.desktop.repository.exception.PersistenceException;
import de.qabel.desktop.ui.AbstractControllerTest;
import de.qabel.desktop.ui.accounting.AccountingView;
import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Locale;

import static com.sun.org.apache.xerces.internal.util.PropertyState.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;


public class ContactControllerTest extends AbstractControllerTest {

	private static final String TEST_FOLDER = "tmp/test";
	private static final String TEST_JSON = "/TestContacts.json";
	private static final String TEST_ALIAS = "TestAlias";

	ContactController controller;

	@After
	public void after() throws Exception {
		//FileUtils.deleteDirectory(new File(TEST_FOLDER));
	}

	@Test
	public void injectTest() {
		Locale.setDefault(new Locale("de", "DE"));
		ContactView view = new ContactView();
		Identity i = new Identity("test", null, new QblECKeyPair());
		clientConfiguration.selectIdentity(i);
		clientConfiguration.setAccount(new Account("Provider", "user", "auth"));
		controller = (ContactController) view.getPresenter();
		assertNotNull(controller);
	}

	@Test
	public void exportContactsTest() throws URISyntaxException, EntityNotFoundExcepion, IOException, QblDropInvalidURL, PersistenceException {
		Identity i = identityBuilderFactory.factory().withAlias(TEST_ALIAS).build();
		clientConfiguration.selectIdentity(i);

		Contact c = new Contact(i, i.getAlias(), i.getDropUrls(), i.getEcPublicKey());
		c.setPhone("000");
		c.setEmail("abc");
		contactRepository.save(c);

		controller = getController();
		File testDir = new File(TEST_FOLDER);
		testDir.mkdirs();
		File file = new File(testDir + "/contacts.json");
		controller.exportContacts(file);
		List<Contact> list = contactRepository.findAllContactFormOneIdentity(i);
		assertEquals(1, list.size());

		controller.importContacts(file);

		list = contactRepository.findAllContactFormOneIdentity(i);
		assertEquals(2, list.size());

		Contact contact0 = list.get(0);
		Contact contact1 = list.get(1);

		assertEquals(contact0.getAlias(), contact1.getAlias());
		assertEquals(contact0.getEmail(), contact1.getEmail());
		assertEquals(contact0.getCreated(), contact1.getCreated(), 100000);
		assertEquals(contact0.getDeleted(), contact1.getDeleted());
		assertEquals(contact0.getUpdated(), contact1.getUpdated());
		assertEquals(contact0.getDropUrls(), contact1.getDropUrls());
		assertEquals(contact0.getEcPublicKey(), contact1.getEcPublicKey());
	}

	@Test
	public void importContactsTest() throws URISyntaxException, PersistenceException, IOException, QblDropInvalidURL, EntityNotFoundExcepion {
		Identity i = identityBuilderFactory.factory().withAlias(TEST_ALIAS).build();
		clientConfiguration.selectIdentity(i);
		File f = new File(System.class.getResource(TEST_JSON).toURI());
		controller = getController();
		controller.importContacts(f);



		List<Contact> list = contactRepository.findAllContactFormOneIdentity(i);
		assertEquals(3, list.size());

		Contact c = list.get(0);
		assertEquals(c.getAlias(),"Qabel");
		assertEquals(c.getEmail(), "mail.awesome@qabel.de");
		assertEquals(c.getDropUrls().size(), 1);
		assertNotNull(c.getEcPublicKey());
	}

	private ContactController getController() {
		ContactView view = new ContactView();
		view.getView();
		return (ContactController) view.getPresenter();
	}

}
