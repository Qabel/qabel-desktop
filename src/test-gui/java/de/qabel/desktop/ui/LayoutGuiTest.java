package de.qabel.desktop.ui;

import com.airhacks.afterburner.views.FXMLView;
import de.qabel.core.accounting.BoxClientStub;
import de.qabel.core.accounting.QuotaState;
import de.qabel.core.config.Contact;
import de.qabel.core.crypto.QblECPublicKey;
import de.qabel.core.drop.DropMessage;
import de.qabel.core.exceptions.QblInvalidCredentials;
import de.qabel.desktop.ui.actionlog.PersistenceDropMessage;
import org.junit.Test;

import java.io.IOException;
import java.util.HashSet;

import static org.junit.Assert.*;

public class LayoutGuiTest extends AbstractGuiTest<LayoutController> {

    @Override
    protected FXMLView getView() {
        return new LayoutView();
    }

    private LayoutController createController() {
        LayoutView view = new LayoutView();
        return (LayoutController) view.getPresenter();
    }

    @Test
    public void hideQuotaBarWhenGetQuotaFails() {
        ((BoxClientStub) controller.boxClient).ioException = new IOException("LayoutGuiTest");
        controller.quotaState = null;
        runLaterAndWait(controller::fillQuotaInformation);
        assertFalse(controller.quotaBlock.isVisible());
        assertFalse(controller.quotaDescription.isVisible());

    }

    @Test
    public void showsQuotaBarWith0MinWidth() throws IOException, QblInvalidCredentials {
        controller.quotaState = new QuotaState(100, 100);
        runLaterAndWait(controller::fillQuotaInformation);
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
}
