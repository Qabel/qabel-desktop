package de.qabel.desktop.ui.accounting.identity;

import de.qabel.core.repository.IdentityRepository;
import de.qabel.core.repository.exception.PersistenceException;
import de.qabel.desktop.ui.AbstractControllerTest;
import org.junit.Before;
import org.junit.Test;

import static junit.framework.Assert.assertEquals;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
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
        identity.setUploadEnabled(true);
        identity.setAlias("a");
        identity.setEmail("b");
        identity.setPhone("c");
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
    public void loadsIdentitiesValues() {
        assertThat(controller.isPrivate(), is(false));
        assertThat(controller.getAlias(), equalTo("a"));
        assertThat(controller.getEmail(), equalTo("b"));
        assertThat(controller.getPhone(), equalTo("c"));
    }

    @Test
    public void loadsPrivateIdentities() {
        identity.setUploadEnabled(false);
        createController();
        assertThat(controller.isPrivate(), is(true));
    }

    @Test
    public void canUpdateIdentity() throws Exception {
        controller.identityRepository = spy(identityRepository);

        controller.setAlias(ALIAS);
        controller.setEmail(EMAIL);
        controller.setPhone(PHONE);
        controller.setPrivate(true);
        controller.saveIdentity();

        assertEquals(ALIAS, identity.getAlias());
        assertEquals(EMAIL, identity.getEmail());
        assertEquals(PHONE, identity.getPhone());
        assertEquals(false, identity.isUploadEnabled());
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

    @Test
    public void saveButtonSwitchesLabels() throws Exception {
        controller.setPrivate(true);
        assertThat(controller.saveIdentity.getText(), equalTo("Save"));
        assertThat(controller.privateHint.getText(), equalTo("private description text"));
        assertThat(controller.privateLabel.getText(), equalTo("Private"));

        controller.setPrivate(false);
        assertThat(controller.saveIdentity.getText(), equalTo("Save & Publish"));
        assertThat(controller.privateHint.getText(), equalTo("public description text"));
        assertThat(controller.privateLabel.getText(), equalTo("Public"));
    }
}
