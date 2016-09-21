package de.qabel.desktop.ui.accounting.identitycontextmenu;

import com.airhacks.afterburner.views.FXMLView;
import de.qabel.desktop.ui.AbstractGuiTest;
import org.junit.Test;

import static de.qabel.desktop.AsyncUtils.assertAsync;


public class IdentityContextMenuGuiTest extends AbstractGuiTest<IdentityContextMenuController> {
    private IdentityContextMenuPage page;

    @Override
    public void setUp() throws Exception {
        super.setUp();
        page = new IdentityContextMenuPage(baseFXRobot, robot, controller);
        controller.identityContextMenu.setVisible(true);
        controller.layoutWindow = controller.identityContextMenu;
    }

    @Override
    protected FXMLView getView() {
        return new IdentityContextMenuView(identity);
    }

    @Test
    public void canShowIdentityEdit() throws Exception {
        page.openIdentityEdit().pressSave();
    }

    @Test
    public void openQr() {
        page.openQrCode();
        assertAsync(page::qrcodeIsVisible);
    }
}
