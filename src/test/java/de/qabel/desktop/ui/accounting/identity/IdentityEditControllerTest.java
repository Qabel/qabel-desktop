package de.qabel.desktop.ui.accounting.identity;

import de.qabel.desktop.ui.AbstractControllerTest;
import org.junit.Before;
import org.junit.Test;

import static junit.framework.Assert.assertEquals;

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

    private void setIdentityProperties() {
        controller.setAlias(ALIAS);
        controller.setEmail(EMAIL);
        controller.setPhone(PHONE);
    }

    private void createController() {
        IdentityEditView view = new IdentityEditView(generateInjection("identity", identity));
        controller = (IdentityEditController) view.getPresenter();
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
    public void canUpdateIdentity() {
        setIdentityProperties();
        controller.saveIdentity();
        assertEquals(ALIAS, identity.getAlias());
    }

    @Test
    public void canSaveIdentity() {
        setIdentityProperties();
        controller.identityRepository = new IdentityRepositoryFake();
        controller.saveIdentity();

        runLaterAndWait(() -> controller.alert.getAlert().isShowing());
    }


}
