package de.qabel.desktop.ui.accounting.identitycontextmenu;

import de.qabel.desktop.ui.AbstractControllerTest;
import org.junit.Before;
import org.junit.Test;

import java.util.function.Function;

import static org.junit.Assert.assertNotNull;

public class IdentityContextMenuControllerTest extends AbstractControllerTest {

    private IdentityContextMenuController controller;

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();
        controller = getController();
    }

    @Test
    public void canCreateController() {
        assertNotNull(controller);
    }

    private Function<String, Object> getIdentityInjectionContext() {
        return generateInjection("identity", identity);
    }

    private IdentityContextMenuController getController() {
        IdentityContextMenuView view = new IdentityContextMenuView(getIdentityInjectionContext());
        view.getView();
        return (IdentityContextMenuController) view.getPresenter();
    }

}
