package de.qabel.desktop.ui.accounting;

import de.qabel.core.config.Identity;
import de.qabel.desktop.config.factory.DropUrlGenerator;
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

	String json = "{\"alias\":\"My Name\",\"keys\":{\"curve25519\":{},\"privateKey\":[10,81,75,-68,-37,13,112,-106,-91,97,6,57,50,126,83,-67,-18,-41,-122,-6,60,-114,32,-83,60,106,117,-36,-107,27,114,-11],\"pubKey\":{\"pubKey\":[72,3,-55,4,58,-122,89,-21,-76,-34,36,-37,-78,30,-122,-105,3,95,31,118,-79,-82,-49,119,9,-96,-47,121,94,-77,5,120]}},\"dropUrls\":[{\"uri\":\"http://localhost:5000/OxIA_fJULczG792cZJ2kE66E_OWa45HhlU-RW2GzfAA\"}],\"moduleSettings\":[],\"id\":0,\"created\":1452602138537,\"updated\":0,\"deleted\":0,\"persistenceID\":\"66a36cd4-2019-4fea-95a2-72c9c45e52cd\"}\n";
	AccountingController controller = new AccountingController();
	private static final String TMP_DIR = "tmp";
	private static final String Test_DIR = "test";
	private static final String TEST_ALIAS = "TestAlias";

	@After
	public void after() throws Exception {
		FileUtils.deleteDirectory(new File(TMP_DIR + "/" + Test_DIR));
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
	public void saveFileTest() throws IOException, QblStorageException {
		File testDir = new File(TMP_DIR + "/" + Test_DIR);
		testDir.mkdirs();
		controller.saveFile(json, TEST_ALIAS, testDir);
		File testFile = new File(TMP_DIR + "/" + Test_DIR + "/" + TEST_ALIAS + ".json");
		String result = controller.readFile(testFile);
		assertEquals(result, json);
	}
}