package de.qabel.desktop.ui.contact;

import com.airhacks.afterburner.views.FXMLView;
import de.qabel.core.config.Contact;
import de.qabel.core.config.Identity;
import de.qabel.core.repository.exception.PersistenceException;
import de.qabel.desktop.ui.AbstractGuiTest;
import org.junit.Test;

import static de.qabel.desktop.AsyncUtils.assertAsync;
import static junit.framework.TestCase.assertEquals;
import static org.hamcrest.Matchers.is;

public class ContactGuiTest extends AbstractGuiTest<ContactController> {

    int namingElements = 1;

    @Override
    protected FXMLView getView() {
        return new ContactView();
    }

    @Test
    public void testDeleteContact() throws Exception {
        ContactPage page = new ContactPage(baseFXRobot, robot, controller);

        Identity identity = identityBuilderFactory.factory().withAlias("MainIdentity").build();
        createNewContactAndSaveInRepo("1", identity);
        createNewContactAndSaveInRepo("2", identity);
        createNewContactAndSaveInRepo("3", identity);

        runLaterAndWait(() -> clientConfiguration.selectIdentity(identity));
        int elements = contactRepository.find(identity).getContacts().size();

        runLaterAndWait(controller::update);
        page.getFirstItem().delete();

        assertAsync(controller.contactList.getChildren()::size, is(elements + namingElements - 1));
    }

    private void createNewContactAndSaveInRepo(String name, Identity identity) throws PersistenceException {
        Identity i3 = identityBuilderFactory.factory().withAlias("I" + name).build();
        Contact c3 = new Contact("C" + name, i3.getDropUrls(), i3.getEcPublicKey());
        contactRepository.save(c3, identity);
    }

    @Test
    public void testOpenMenu() {
        ContactPage page = new ContactPage(baseFXRobot, robot, controller);
        controller.layoutWindow = controller.contactList;
        page.openMenu();
        waitUntil(() -> controller.contactMenuController != null);
        waitUntil(() -> controller.contactMenuController.popOver != null);
        assertAsync(() -> controller.contactMenuController.popOver.isShowing());
    }

    @Test
    public void testDummyContact() throws Exception {
        assertEquals(1, controller.contactList.getChildren().size());
    }
}
