package de.qabel.desktop.ui.accounting.identitycontextmenu;

import de.qabel.desktop.ui.AbstractControllerTest;
import javafx.scene.layout.Pane;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertNotNull;

public class IdentityContextMenuControllerTest extends AbstractControllerTest {

    private IdentityContextMenuController controller;

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();
        controller = createController();
    }

    private IdentityContextMenuController createController() {
        IdentityContextMenuView view = new IdentityContextMenuView(identity);
        view.getView();
        return (IdentityContextMenuController) view.getPresenter();
    }

    @Test
    public void canCreateController() {
        assertNotNull(controller);
    }

    @Test
    public void canCreateIdentityEditView() {
        controller.createIdentityEdit(new Pane());
        assertNotNull(controller.identityEditView);
    }
}
