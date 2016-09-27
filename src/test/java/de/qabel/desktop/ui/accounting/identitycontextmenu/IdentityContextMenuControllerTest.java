package de.qabel.desktop.ui.accounting.identitycontextmenu;

import de.qabel.desktop.ui.AbstractControllerTest;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.scene.control.ButtonType;
import javafx.scene.layout.Pane;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

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

    @Test
    public void deletesIdentities() throws Exception {
        Platform.runLater(controller::delete);
        waitUntil(() -> controller.getConfirmDialog() != null);

        confirm(ButtonType.YES);
        assertFalse(identityRepository.findAll().contains(identity));
    }

    protected void confirm(ButtonType yes) {
        runLaterAndWait(() ->
            controller.getConfirmDialog().getDialogPane().lookupButton(yes).fireEvent(new ActionEvent()));
        runLaterAndWait(() -> {});
        waitUntil(() -> controller.getConfirmDialog() == null);
    }

    @Test
    public void canAbortIdentityDeletion() throws Exception {
        Platform.runLater(controller::delete);
        waitUntil(() -> controller.getConfirmDialog() != null);

        confirm(ButtonType.CANCEL);
        assertTrue(identityRepository.findAll().contains(identity));
    }
}
