package de.qabel.desktop.ui;

import com.airhacks.afterburner.views.FXMLView;
import de.qabel.core.accounting.BoxClientStub;
import de.qabel.core.accounting.QuotaState;
import de.qabel.core.config.Contact;
import de.qabel.core.crypto.QblECPublicKey;
import de.qabel.core.drop.DropMessage;
import de.qabel.core.exceptions.QblInvalidCredentials;
import de.qabel.desktop.ui.about.AboutController;
import de.qabel.desktop.ui.about.AboutPage;
import de.qabel.desktop.ui.accounting.identitycontextmenu.IdentityContextMenuController;
import de.qabel.desktop.ui.accounting.identitycontextmenu.IdentityContextMenuView;
import de.qabel.desktop.ui.actionlog.PersistenceDropMessage;
import org.junit.Test;

import java.io.IOException;
import java.util.HashSet;

import static org.junit.Assert.*;

public class LayoutGuiTest extends AbstractGuiTest<LayoutController> {

    private QuotaState quotaState;
    private AboutPage aboutPage;

    @Override
    protected FXMLView getView() {
        return new LayoutView();
    }

    private LayoutController createController() {
        LayoutView view = new LayoutView();
        return (LayoutController) view.getPresenter();
    }

    @Test
    public void showAboutViewOnStartIfAnIdentityExists() {
        waitUntil(() -> controller.aboutView != null);
        aboutPage = new AboutPage(baseFXRobot, robot, (AboutController) controller.aboutView.getPresenter());
        aboutPage.waitForRootNode();
    }

    @Test
    public void hideQuotaBarWhenGetQuotaFails() {
        ((BoxClientStub) controller.boxClient).ioException = new IOException("LayoutGuiTest");
        runLaterAndWait(() -> {
            controller.fillQuotaInformation(controller.getQuotaState());
        });
        assertFalse(controller.quotaBlock.isVisible());
        assertFalse(controller.quotaDescription.isVisible());
    }

    @Test
    public void showsQuotaBarWith0MinWidth() throws IOException, QblInvalidCredentials {
        quotaState = new QuotaState(100, 100);
        runLaterAndWait(() -> {
            controller.fillQuotaInformation(quotaState);
        });
        assertEquals(0, (int) controller.quotaBar.getMinWidth());
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
    public void testInviteMenuStyle() throws Exception {
        clickOn(controller.inviteButton);
        waitUntil(controller.inviteBackground::isVisible);
        assertTrue(controller.inviteBackground.isVisible());
        assertTrue(controller.inviteButton.getStyleClass().contains("darkgrey"));
    }

    @Test
    public void testCleanMenuIcons() throws Exception {
        clickOn(controller.browseNav);
        assertFalse(controller.inviteBackground.isVisible());
        assertFalse(controller.faqBackground.isVisible());
        assertFalse(controller.feedbackBackground.isVisible());
    }

    @Test
    public void testUnactivatedNaviItem() throws Exception {
        clickOn(controller.browseNav);
        clickOn(controller.inviteButton);
        assertFalse(controller.browseNav.button.getGraphic().getStyleClass().contains("active"));
    }

    @Test
    public void testIconsNaviItem() throws Exception {
        assertTrue(controller.browseNav.button.getGraphic() != null);
        assertTrue(controller.contactsNav.button.getGraphic() != null);
        assertTrue(controller.syncNav.button.getGraphic() != null);
        assertTrue(controller.accountingNav.button.getGraphic() != null);
        assertTrue(controller.aboutNav.button.getGraphic() != null);
    }
}
