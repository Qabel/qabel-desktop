package de.qabel.desktop.ui;

import com.airhacks.afterburner.views.FXMLView;
import de.qabel.core.config.Contact;
import de.qabel.core.crypto.QblECPublicKey;
import de.qabel.core.drop.DropMessage;
import de.qabel.desktop.ui.actionlog.PersistenceDropMessage;
import org.junit.Test;

import java.util.HashSet;

import static org.junit.Assert.assertTrue;

public class LayoutGuiTest extends AbstractGuiTest<LayoutController> {
    @Override
    protected FXMLView getView() {
        return new LayoutView();
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
