package de.qabel.desktop.ui.accounting.identitycontextmenu;

import de.qabel.desktop.ui.AbstractControllerTest;
import de.qabel.desktop.ui.accounting.identity.IdentityEditController;
import de.qabel.desktop.ui.accounting.identity.IdentityEditView;
import javafx.scene.layout.Pane;
import org.junit.Before;
import org.junit.Test;

import java.util.function.Function;

import static org.junit.Assert.assertNotNull;

public class IdentityContextMenuControllerTest extends AbstractControllerTest {

    private IdentityContextMenuController controller;

    private IdentityEditView identityEditView;
    private IdentityEditController identityEditController;


    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();
        controller = createController();
        controller.layoutWindow = new Pane();
    }

    private Function<String, Object> getIdentityInjectionContext() {
        return generateInjection("identity", identity);
    }

    private IdentityContextMenuController createController() {
        IdentityContextMenuView view = new IdentityContextMenuView(getIdentityInjectionContext());
        view.getView();
        return (IdentityContextMenuController) view.getPresenter();
    }

    @Test
    public void canCreateController() {
        assertNotNull(controller);
    }

    @Test
    public void canCreateIdentityEditView() {
        identityEditView = controller.createIdentityEdit(new Pane());
        assertNotNull(identityEditView);
    }

    @Test
    public void canOpenEdit() {
        controller.openIdentityEdit();
        runLaterAndWait(() -> controller.identityEditController.isShowing());
    }
}
