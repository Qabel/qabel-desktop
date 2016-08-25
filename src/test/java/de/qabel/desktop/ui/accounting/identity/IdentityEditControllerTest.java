package de.qabel.desktop.ui.accounting.identity;

import de.qabel.core.repository.exception.PersistenceException;
import de.qabel.desktop.ui.AbstractControllerTest;
import org.junit.Before;
import org.junit.Test;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;

public class IdentityEditControllerTest extends AbstractControllerTest {

    private IdentityEditController controller;

    static String ALIAS = "IDC Alias";
    static String EMAIL = "idcalias@examplemail.com";
    static String PHONE = "0121469465419";

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();
        createController();
    }

    @Test
    public void canCreateController() {
        assertNotNull(controller);
    }

    @Test
    public void canSetAlias() {
        controller.setAlias(ALIAS);
        assertEquals(ALIAS, controller.getAlias());
    }

    @Test
    public void canSetEmail() {
        controller.setEmail(EMAIL);
        assertEquals(EMAIL, controller.getEmail());
    }

    @Test
    public void canSetPhone() {
        controller.setPhone(PHONE);
        assertEquals(PHONE, controller.getPhone());
    }

    @Test
    public void canUpdateIdentity() throws PersistenceException {
        controller.setAlias(ALIAS);
        controller.setEmail(EMAIL);
        controller.setPhone(PHONE);

        controller.updateIdentity();

        assertEquals(ALIAS, identity.getAlias());
        assertEquals(EMAIL, identity.getEmail());
        assertEquals(PHONE, identity.getPhone());
    }

    private void createController() {
        IdentityEditView view = new IdentityEditView(generateInjection("identity", identity));
        controller = (IdentityEditController) view.getPresenter();
    }

}
