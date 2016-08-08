package de.qabel.desktop.ui;

import com.airhacks.afterburner.views.FXMLView;
import de.qabel.core.config.Contact;
import de.qabel.core.crypto.QblECPublicKey;
import de.qabel.core.drop.DropMessage;
import de.qabel.desktop.ui.accounting.identitycontextmenu.IdentityContextMenuController;
import de.qabel.desktop.ui.accounting.identitycontextmenu.IdentityContextMenuView;
import de.qabel.desktop.ui.actionlog.PersistenceDropMessage;
import javafx.application.Platform;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import org.junit.Ignore;
import org.junit.Test;

import java.util.HashSet;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class LayoutGuiTest extends AbstractGuiTest<LayoutController> {

    @Override
    protected FXMLView getView() {
        return new LayoutView();
    }

    private LayoutController createController() {
        LayoutView view = new LayoutView();
        return (LayoutController) view.getPresenter();
    }

    @Ignore
    @Test
    public void showQuotaBarWithDummyText() throws InterruptedException {
        LayoutController conroller = createController();

        //// TODO: 21.07.16 set the values from real BoxClient and then implement it in layout
        controller.quota.setText("24");
        controller.provider.setText("24");
        controller.quotaDescription.setText("1,7 GB free / 2 GB");

    }

    @Test
    public void testStuff() throws Exception {
        Contact sender = new Contact("test", new HashSet<>(), new QblECPublicKey("test".getBytes()));
        contactRepository.save(sender, identity);
        DropMessage dropMessage = new DropMessage(sender, "message", "plaintext");
        PersistenceDropMessage message = new PersistenceDropMessage(dropMessage, sender, identity, false, false);

        Indicator indicator = controller.contactsNav.getIndicator();
        dropMessageRepository.save(message);
        waitUntil(() -> indicator.getText().equals("1"));
        assertTrue(indicator.isVisible());
    }

    @Test
    public void testObservableIdentitty() throws Exception {
        IdentityContextMenuView menuView = new IdentityContextMenuView(generateInjection("identity", identity));
        IdentityContextMenuController menuController = (IdentityContextMenuController) menuView.getPresenter();
        menuController.setAlias("new alias identity");
        assertEquals("new alias identity", identity.getAlias());
    }


    @Test
    public void testInviteMenuStyle() throws Exception {
        clickOn(controller.inviteButton);
        waitUntil(controller.inviteBackground::isVisible);
        assertTrue(controller.inviteBackground.isVisible());
        assertEquals(controller.inviteButton.getStyle(), "-fx-effect: innershadow(gaussian, #222222, 10, 10, 10, 10);");
    }

    @Test
    public void testCleanMenuIcons() throws Exception {
        clickOn(controller.browseNav);
        assertTrue(!controller.inviteBackground.isVisible());
        assertTrue(!controller.faqBackground.isVisible());
        assertTrue(!controller.feedbackBackground.isVisible());
    }
}
