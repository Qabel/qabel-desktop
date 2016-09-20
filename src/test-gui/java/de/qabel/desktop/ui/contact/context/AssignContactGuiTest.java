package de.qabel.desktop.ui.contact.context;

import com.airhacks.afterburner.views.FXMLView;
import com.sun.javafx.robot.FXRobot;
import de.qabel.core.config.Contact;
import de.qabel.core.config.Identity;
import de.qabel.desktop.ui.AbstractGuiTest;
import de.qabel.desktop.ui.AbstractPage;
import org.junit.Test;
import org.testfx.api.FxRobot;

public class AssignContactGuiTest extends AbstractGuiTest<AssignContactController> {
    private Contact contact;
    private AssignContactPage page;

    @Override
    protected FXMLView getView() {
        contact = identityBuilderFactory.factory().withAlias("contact1").build().toContact();
        return new AssignContactView(contact);
    }

    @Override
    public void setUp() throws Exception {
        super.setUp();
        page = new AssignContactPage(baseFXRobot, robot);
    }

    @Test
    public void testAssignmentAndUnassignment() throws Exception {
        page.toggle(identity);
        waitUntil(() ->
            contactRepository.exists(contact) &&
            contactRepository.findContactWithIdentities(contact.getKeyIdentifier())
                .getIdentities()
                .contains(identity));
    }

    private class AssignContactPage extends AbstractPage {
        public AssignContactPage(FXRobot baseFXRobot, FxRobot robot) {
            super(baseFXRobot, robot);
        }

        public void toggle(Identity identity) {
            clickOn("#assign-" + identity.getId());
        }
    }
}
