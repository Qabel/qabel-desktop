package de.qabel.desktop.ui;

import com.airhacks.afterburner.views.FXMLView;
import de.qabel.core.config.Contact;
import de.qabel.core.crypto.QblECPublicKey;
import de.qabel.core.drop.DropMessage;
import de.qabel.desktop.ui.actionlog.PersistenceDropMessage;
import javafx.fxml.FXML;
import org.junit.Test;

import java.awt.*;
import java.util.HashSet;

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

    @Test
    public void showQuotaBarWithDummyText() throws InterruptedException {
        LayoutController conroller = createController();

        //// TODO: 21.07.16 set the values from real BoxClient and then implement it in layout
        controller.quota.setText("24");
        controller.provider.setText("24");
        controller.quotaDescription.setText("1,7 GB free / 2 GB");

        Thread.sleep(30000);
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
