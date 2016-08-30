package de.qabel.desktop.ui.accounting.identity;

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

    private void setIdentityProperties() {
        controller.setAliasField(ALIAS);
        controller.setEmailField(EMAIL);
        controller.setPhoneField(PHONE);
    }

    private void createController() {
        IdentityEditView view = new IdentityEditView(generateInjection("identity", identity));
        controller = (IdentityEditController) view.getPresenter();
    }

    @Test
    public void canSetAlias() {
        controller.setAliasField(ALIAS);
        assertEquals(ALIAS, controller.getAliasField());
    }

    @Test
    public void canSetEmail() {
        controller.setEmailField(EMAIL);
        assertEquals(EMAIL, controller.getEmailField());
    }

    @Test
    public void canSetPhone() {
        controller.setPhoneField(PHONE);
        assertEquals(PHONE, controller.getPhoneField());
    }

    @Test
    public void canUpdateIdentity() {
        setIdentityProperties();
        controller.updateIdentity();
        assertEquals(ALIAS, identity.getAlias());
    }

    @Test
    public void canSaveIdentity() {
        setIdentityProperties();
        controller.identityRepository = new IdentityRepositoryFake();
        controller.saveIdentity(identity);

        runLaterAndWait(() -> controller.alert.getAlert().isShowing());
    }


}
