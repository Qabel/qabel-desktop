package de.qabel.desktop.ui.accounting.identitycontextmenu;

import com.airhacks.afterburner.views.FXMLView;
import de.qabel.core.config.Identities;
import de.qabel.core.config.Identity;
import de.qabel.core.crypto.QblECKeyPair;
import de.qabel.desktop.config.factory.DropUrlGenerator;
import de.qabel.desktop.config.factory.IdentityBuilder;
import de.qabel.desktop.ui.AbstractGuiTest;
import de.qabel.desktop.ui.accounting.identityContextMenu.IdentityContextMenuController;
import de.qabel.desktop.ui.accounting.identityContextMenu.IdentityContextMenuView;
import org.junit.Before;
import org.junit.Test;

import java.net.URISyntaxException;

import static org.junit.Assert.assertEquals;


public class IdentityContextMenuGuiTest extends AbstractGuiTest<IdentityContextMenuController> {
    private Identity identity;
    private IdentityContextMenuPage page;

    @Before
    @Override
    public void setUp() throws Exception {
        super.setUp();
        page = new IdentityContextMenuPage(baseFXRobot, robot, controller);
    }

    @Override
    protected FXMLView getView() {
        try {
            identity = new IdentityBuilder(new DropUrlGenerator("http://localhost")).withAlias("alias").build();
        } catch (URISyntaxException e) {
            throw new IllegalStateException("won't happen", e);
        }
        return new IdentityContextMenuView(generateInjection("identity", identity));
    }

    @Test
    public void testEdit() throws Exception {
        page.edit().inputAndConfirm("new alias");
        assertEquals("new alias", controller.alias.getText());
    }

    @Test
    public void savesAlias() throws Exception {
        Identity i = new Identity("alias", null, new QblECKeyPair());
        controller.setAlias("new alias");
        assertEquals("new alias", controller.alias.getText());
        Identities results = identityRepository.findAll();
        Identity identity = results.getByKeyIdentifier(i.getKeyIdentifier());
        assertEquals("new alias", identity.getAlias());
    }




}
