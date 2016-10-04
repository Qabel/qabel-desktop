package de.qabel.desktop.ui.accounting;

import de.qabel.box.storage.exceptions.QblStorageException;
import de.qabel.core.config.Identities;
import de.qabel.core.config.Identity;
import de.qabel.core.exceptions.QblDropInvalidURL;
import de.qabel.core.repository.exception.EntityNotFoundException;
import de.qabel.core.repository.exception.PersistenceException;
import de.qabel.desktop.action.IdentityDeletedEvent;
import de.qabel.desktop.ui.AbstractControllerTest;
import de.qabel.desktop.ui.accounting.item.AccountingItemController;
import org.apache.commons.io.FileUtils;
import org.json.JSONException;
import org.junit.After;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;

import static org.junit.Assert.*;


public class AccountingControllerTest extends AbstractControllerTest {

    private static final String TMP_DIR = "tmp";
    private static final String TEST_DIR = "test";
    private static final String TEST_FOLDER = TMP_DIR + "/" + TEST_DIR;
    private static final String TEST_ALIAS = "TestAlias";
    private static final String TEST_JSON = "/TestIdentity.json";

    AccountingController controller;
    private Identity newIdentity;

    @Override
    public void setUp() throws Exception {
        super.setUp();
        newIdentity = identityBuilderFactory.factory().withAlias("new").build();
    }

    @After
    public void after() throws Exception {
        FileUtils.deleteDirectory(new File(TEST_FOLDER));
    }

    @Test
    public void showsIdentities() throws Exception {
        AccountingController controller = getAccountingController();

        assertEquals(1, controller.identityList.getChildren().size());
        assertEquals(1, controller.itemViews.size());

        assertEquals(
            identity.getKeyIdentifier(),
            ((AccountingItemController) controller.itemViews.get(0).getPresenter()).getIdentity().getKeyIdentifier()
        );
    }

    private AccountingController getAccountingController() {
        AccountingView view = new AccountingView();
        view.getView();
        return (AccountingController) view.getPresenter();
    }

    @Test
    public void importIdentityTest() throws IOException, QblStorageException, PersistenceException, EntityNotFoundException, URISyntaxException, QblDropInvalidURL, JSONException {
        setupExport();
        controller.importIdentity(new File(System.class.getResource(TEST_JSON).toURI()));
        Identities identities = identityRepository.findAll();

        assertEquals(2, identities.getIdentities().size());
        Identity i = identities.getByKeyIdentifier("1b72b39576ced4ac8e003fae36d96dbda94ab28b2bdaf399719e1402fea9210c");
        assertEquals("Test", i.getAlias());
    }

    @Test
    public void refreshesOnIdentityDeletion() throws Exception {
        identityRepository.save(newIdentity);
        controller = getAccountingController();
        identityRepository.delete(newIdentity);
        eventDispatcher.push(new IdentityDeletedEvent(newIdentity));

        waitUntil(() -> controller.identityList.getChildren().size() == 1);
    }

    @Test
    public void refreshesOnIdentityAdd() throws Exception {
        controller = getAccountingController();
        identityRepository.save(newIdentity);

        waitUntil(() -> controller.identityList.getChildren().size() == 2);
    }

    private File setupExport() throws URISyntaxException {
        controller = getAccountingController();
        File testDir = new File(TEST_FOLDER);
        testDir.mkdirs();
        return testDir;
    }
}
