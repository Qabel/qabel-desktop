package de.qabel.desktop.ui.accounting.qrcode;

import com.airhacks.afterburner.views.FXMLView;
import de.qabel.core.config.Identity;
import de.qabel.core.config.factory.DropUrlGenerator;
import de.qabel.core.config.factory.IdentityBuilder;
import de.qabel.desktop.ui.AbstractGuiTest;
import org.junit.Before;
import org.junit.Test;

import java.net.URISyntaxException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class QRCodeGuiTest extends AbstractGuiTest<QRCodeController> {
    private QRCodePage page;
    private Identity identity;

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();
        page = new QRCodePage(baseFXRobot, robot, controller);
    }

    @Override
    protected FXMLView getView() {
        try {
            identity = new IdentityBuilder(new DropUrlGenerator("http://localhost")).withAlias("alias").build();
        } catch (URISyntaxException e) {
            throw new IllegalStateException("won't happen", e);
        }
        return new QRCodeView(generateInjection("identity", identity));
    }

    @Test
    public void showPopup() throws Exception {
        page.showPopup();
        String textQRCode = "QABELCONTACT\n"
            + "alias" + "\n"
            + controller.getDropUrl() + "\n"
            + controller.getPublicKey();

        assertEquals("alias", controller.getAlias());
        assertTrue(controller.getDropUrl().startsWith("http://localhost:5000" + "/"));
        assertEquals(textQRCode, controller.getTextQRCode());
    }

}
