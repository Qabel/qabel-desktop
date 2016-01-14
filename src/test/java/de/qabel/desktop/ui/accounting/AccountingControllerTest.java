package de.qabel.desktop.ui.accounting;

import de.qabel.core.config.Identity;
import de.qabel.core.exceptions.QblDropInvalidURL;
import de.qabel.desktop.config.factory.IdentityBuilder;
import de.qabel.desktop.config.factory.IdentityBuilderFactory;
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


public class AccountingControllerTest extends AbstractControllerTest {

	private static final String TMP_DIR = "tmp";
	private static final String TEST_DIR = "test";
	private static final String TEST_FOLDER = TMP_DIR + "/" + TEST_DIR;
	private static final String TEST_ALIAS = "TestAlias";
	private static final String TEST_JSON = "/Test.json";

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
	public void exportTest() throws IOException, QblStorageException, URISyntaxException {
		File testDir = setupImportAndExport();
		identity = new Identity(TEST_ALIAS, null, null);
		controller.exportIdentity(identity, testDir);
		File f = new File(TEST_FOLDER + "/" + TEST_ALIAS + ".json");
		assertEquals(f.getName(), TEST_ALIAS+ ".json");

		String contentNew = controller.readFile(f);

		String contentFixture = controller.readFile(new File(System.class.getResource(TEST_JSON).toURI()));
		assertEquals(f.getName(), TEST_ALIAS + ".json");
		System.out.print(contentNew);
		assertEquals(contentNew.substring(0,30), contentFixture.substring(0,30));
	}

	@Test
	public void importTest() throws IOException, QblStorageException, PersistenceException, EntityNotFoundExcepion, URISyntaxException {
		setupImportAndExport();
		controller.importIdentity(new File(System.class.getResource(TEST_JSON).toURI()));
		List<Identity> identities = identityRepository.findAll();

		assertEquals(1, identities.size());
		assertEquals("My Name", identities.get(0).getAlias());

	}

	@Test
	public void validateSchemaTest() throws URISyntaxException, IOException, QblStorageException, PersistenceException, EntityNotFoundExcepion, QblDropInvalidURL {
		File testDir = setupImportAndExport();
		Identity identity = identityBuilderFactory.factory().build();
		controller.exportIdentity(identity, testDir);
		File f = new File(TEST_FOLDER + "/" + identity.getAlias() + ".json");

		controller.importIdentity(f);

		List<Identity> identities = identityRepository.findAll();
		Identity newIdentity = identities.get(0);
		assertEquals(identity.getAlias(), newIdentity.getAlias());
		assertEquals(identity.getEmail(), newIdentity.getEmail());
		assertEquals(identity.getPrimaryKeyPair(), newIdentity.getPrimaryKeyPair());
		assertEquals(identity.getPhone(), newIdentity.getPhone());
		assertEquals(identity.getDropUrls(), newIdentity.getDropUrls());
		assertEquals(identity.getCreated(), newIdentity.getCreated());
		assertEquals(identity.getDeleted(), newIdentity.getDeleted());
		assertEquals(identity.getId(), newIdentity.getId());
	}

	private File setupImportAndExport() throws URISyntaxException {
		controller = getAccountingController();
		File testDir = new File(TEST_FOLDER);
		testDir.mkdirs();
		return testDir;
	}
}