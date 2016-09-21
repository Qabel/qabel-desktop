package de.qabel.desktop.ui.accounting.identity;

import de.qabel.core.repository.IdentityRepository;
import de.qabel.core.repository.exception.PersistenceException;
import de.qabel.desktop.ui.AbstractControllerTest;
import org.junit.Before;
import org.junit.Test;

import static junit.framework.Assert.assertEquals;
import static org.mockito.Mockito.*;

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
        IdentityEditView view = new IdentityEditView(identity);
        controller = view.getPresenter();
    }

    @Test
    public void canUpdateIdentity() throws Exception {
        controller.identityRepository = spy(identityRepository);

        controller.setAlias(ALIAS);
        controller.setEmail(EMAIL);
        controller.setPhone(PHONE);
        controller.saveIdentity();

        assertEquals(ALIAS, identity.getAlias());
        assertEquals(EMAIL, identity.getEmail());
        assertEquals(PHONE, identity.getPhone());
        verify(controller.identityRepository).save(identity);
    }

    @Test
    public void showsAlertIfSaveFailed() throws Exception {
        controller.identityRepository = mock(IdentityRepository.class);
        doThrow(new PersistenceException("fail")).when(controller.identityRepository).save(identity);

        controller.saveIdentity();
        setIdentityProperties();

        runLaterAndWait(() -> controller.alert.getAlert().isShowing());
    }

    @Test
    public void canClearFields() {
        controller.clearFields();

        assertEquals("", controller.getAlias());
        assertEquals("", controller.getEmail());
        assertEquals("", controller.getPhone());
    }
}
