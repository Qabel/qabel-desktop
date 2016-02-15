package de.qabel.desktop.ui.accounting;

import com.google.gson.Gson;
import de.qabel.core.config.Contact;
import de.qabel.core.config.Identity;
import de.qabel.core.exceptions.QblDropInvalidURL;
import de.qabel.desktop.exceptions.QblStorageException;
import de.qabel.desktop.repository.exception.EntityNotFoundExcepion;
import de.qabel.desktop.repository.exception.PersistenceException;
import de.qabel.desktop.ui.AbstractControllerTest;
import de.qabel.desktop.ui.accounting.item.AccountingItemController;
import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;


public class AccountingControllerTest extends AbstractControllerTest {

	private static final String TMP_DIR = "tmp";
	private static final String TEST_DIR = "test";
	private static final String TEST_FOLDER = TMP_DIR + "/" + TEST_DIR;
	private static final String TEST_ALIAS = "TestAlias";
	private static final String TEST_JSON = "/TestIdentity.json";
	private static final String TEST_CONTACT = TEST_ALIAS + "_Contact.json";
	private static final String TEST_IDENTITY = TEST_ALIAS + "_Identity.json";

	AccountingController controller;
	Identity identity;


	@After
	public void after() throws Exception {
		FileUtils.deleteDirectory(new File(TEST_FOLDER));
	}

	@Test
	public void showsIdentities() throws PersistenceException {
		Identity identity = new Identity("alias", null, null);
		identityRepository.save(identity);

		AccountingController controller = getAccountingController();

		assertEquals(1, controller.identityList.getChildren().size());
		assertEquals(1, controller.itemViews.size());

		assertEquals(identity, ((AccountingItemController) controller.itemViews.get(0).getPresenter()).getIdentity());
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

	@Test
	public void importIdentityTest() throws IOException, QblStorageException, PersistenceException, EntityNotFoundExcepion, URISyntaxException, QblDropInvalidURL {
		setupExport();
		controller.importIdentity(new File(System.class.getResource(TEST_JSON).toURI()));
		List<Identity> identities = identityRepository.findAll();

		assertEquals(1, identities.size());
		Identity i =identities.get(0);
		assertEquals(TEST_ALIAS, i.getAlias());
	}

	@Test
	public void exportIdentityTest() throws URISyntaxException, IOException, QblStorageException, PersistenceException, EntityNotFoundExcepion, QblDropInvalidURL {
		File file = setupImport(TEST_IDENTITY);
		Identity identity = identityBuilderFactory.factory().build();

		controller.exportIdentity(identity, file);
		File f = new File(TEST_FOLDER + "/" + TEST_IDENTITY);
		controller.importIdentity(f);

		List<Identity> identities = identityRepository.findAll();
		Identity newIdentity = identities.get(0);
		assertEquals(identity.getAlias(), newIdentity.getAlias());
		assertEquals(identity.getEmail(), newIdentity.getEmail());
		assertEquals(identity.getPrimaryKeyPair(), newIdentity.getPrimaryKeyPair());
		assertEquals(identity.getPhone(), newIdentity.getPhone());
		assertEquals(identity.getDropUrls(), newIdentity.getDropUrls());
		assertEquals(identity.getCreated(), newIdentity.getCreated(), 100000);
		assertEquals(identity.getDeleted(), newIdentity.getDeleted());
		assertEquals(identity.getId(), newIdentity.getId());
	}

	@Test
	public void exportContactTest() throws IOException, QblStorageException, URISyntaxException, QblDropInvalidURL {
		File file = setupImport(TEST_CONTACT);

		Identity identity = identityBuilderFactory.factory().build();
		identity.setAlias(TEST_ALIAS);
		identity.setPhone("000");
		identity.setEmail("abc");

		controller.exportContact(identity, file);

		File f = new File(TEST_FOLDER + "/" + TEST_CONTACT);
		assertEquals(f.getName(), TEST_CONTACT);

		String contentNew = controller.readFile(f);
		Gson gson = new Gson();
		GsonContact exportGsonContact = gson.fromJson(contentNew, GsonContact.class);
		Contact exportContact = controller.gsonContactToContact(exportGsonContact, identity);

		assertEquals(exportContact.getAlias(), identity.getAlias());
		assertEquals(exportContact.getEmail(), identity.getEmail());
		assertEquals(exportContact.getPhone(), identity.getPhone());
		assertEquals(exportContact.getEcPublicKey(), identity.getEcPublicKey());
		assertEquals(exportContact.getDropUrls(), identity.getDropUrls());
		assertEquals(exportContact.getCreated(), identity.getCreated(), 100000);
		assertEquals(exportContact.getUpdated(), identity.getUpdated());
		assertEquals(exportContact.getDeleted(), identity.getDeleted());
	}

	@Test
	public void givenNoIdentity_unusableButtonsGetDisabled() throws Exception {
		clientConfiguration.selectIdentity(null);

		controller = getAccountingController();
		assertTrue(controller.exportIdentity.isDisabled());
		assertTrue(controller.exportContact.isDisabled());
	}

	@Test
	public void givenAnIdentity_usableButtonsAreEnabled() throws Exception {
		controller = getAccountingController();
		assertFalse(controller.exportIdentity.isDisabled());
		assertFalse(controller.exportContact.isDisabled());
	}

	@Test
	public void onIdentitySelection_usableButtonsAreEnabled() throws Exception {
		clientConfiguration.selectIdentity(null);

		controller = getAccountingController();
		clientConfiguration.selectIdentity(identityBuilderFactory.factory().withAlias("test").build());
		assertFalse(controller.exportIdentity.isDisabled());
		assertFalse(controller.exportContact.isDisabled());
	}

	private File setupExport() throws URISyntaxException {
		controller = getAccountingController();
		File testDir = new File(TEST_FOLDER);
		testDir.mkdirs();
		return testDir;
	}

	private File setupImport(String type) throws URISyntaxException {
		File testDir = setupExport();
		File file = new File(testDir + "/" + type);
		return file;
	}
}
