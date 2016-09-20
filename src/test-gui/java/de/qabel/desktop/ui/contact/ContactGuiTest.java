package de.qabel.desktop.ui.contact;

import com.airhacks.afterburner.views.FXMLView;
import de.qabel.core.config.Contact;
import de.qabel.core.config.Identity;
import de.qabel.core.repository.exception.PersistenceException;
import de.qabel.desktop.ui.AbstractGuiTest;
import de.qabel.desktop.ui.contact.context.AssignContactPage;
import org.hamcrest.Matcher;
import org.junit.Before;
import org.junit.Test;

import static de.qabel.desktop.AsyncUtils.assertAsync;
import static de.qabel.desktop.AsyncUtils.runLaterAndWait;
import static junit.framework.TestCase.assertEquals;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;

public class ContactGuiTest extends AbstractGuiTest<ContactController> {

    int namingElements = 1;
    private ContactPage page;

    @Override
    protected FXMLView getView() {
        return new ContactView();
    }

    @Before
    @Override
    public void setUp() throws Exception {
        super.setUp();
        page = new ContactPage(baseFXRobot, robot, controller);
    }

    @Test
    public void testDeleteContact() throws Exception {
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

    @Test
    public void testAssignContactPopupOpens() throws Exception {
        expandStageForPopover();
        createNewContactAndSaveInRepo("1", identity);
        controller.update();

        AssignContactPage assignPage = page.getFirstItem().assign();
        assignPage.waitForIdentity(identity);
        assignPage.waitForIgnore();
    }

    @Test
    public void testUnknownContact() throws Exception {
        styleOfContact(true,  containsString("50%,100%)"));
    }

    @Test
    public void testNormalContact() throws Exception {
        styleOfContact(false, containsString("100%,100%)"));
    }

    private void styleOfContact(boolean unknownContact, Matcher<String> stringMatcher) throws Exception {
        createContact(unknownContact);

        controller.update();
        waitUntil(() -> controller.contactList.getChildren().size() == 1);

        assertAsync(() -> {
            try {
                return page.getFirstItem().getAvatarStyle();
            } catch (Exception e) {
                return "";
            }
        }, stringMatcher);
    }


    private void createContact(boolean unknown) throws Exception {
        Identity i = identityBuilderFactory.factory().withAlias("unknown").build();
        Contact c = new Contact("contact", i.getDropUrls(), i.getEcPublicKey());
        if (unknown) {
            c.setStatus(Contact.ContactStatus.UNKNOWN);
        } else {
            c.setStatus(Contact.ContactStatus.NORMAL);
        }
        contactRepository.save(c, identity);
    }

    private void createNewContactAndSaveInRepo(String name, Identity identity) throws PersistenceException {
        Identity i3 = identityBuilderFactory.factory().withAlias("I" + name).build();
        Contact c3 = new Contact("C" + name, i3.getDropUrls(), i3.getEcPublicKey());
        contactRepository.save(c3, identity);
    }

    @Test
    public void testDummyContact() throws Exception {
        assertEquals(1, controller.contactList.getChildren().size());
    }

    @Test
    public void newContactsFilter() throws Exception {
        createContact(true);
        createContact(true);
        createContact(false);

        runLaterAndWait(() -> clientConfiguration.selectIdentity(identity));
        runLaterAndWait(() -> controller.filterCombo.getSelectionModel().select(controller.showNewContacts));
        controller.update();
        assertAsync(() -> controller.contactItems, hasSize(2));

        runLaterAndWait(() -> controller.filterCombo.getSelectionModel().select(controller.showNormalContacts));
        controller.update();
        assertAsync(() -> controller.contactItems, hasSize(3));
    }

}
