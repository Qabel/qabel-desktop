package de.qabel.desktop.ui.accounting.identity;

import de.qabel.core.repository.IdentityRepository;
import de.qabel.core.repository.exception.PersistenceException;
import de.qabel.desktop.ui.AbstractControllerTest;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

import static junit.framework.Assert.assertEquals;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class IdentityEditControllerTest extends AbstractControllerTest {
    private IdentityEditController controller;
    private Semaphore lock = new Semaphore(1);

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
        lock.acquire();
    }

    private void setIdentityProperties() {
        controller.setAlias(ALIAS);
        controller.setEmail(EMAIL);
        controller.setPhone(PHONE);
        controller.setPrivate(true);
    }

    private void createController() {
        IdentityEditView view = new IdentityEditView(identity);
        controller = view.getPresenter();
    }

    @Test
    public void savesIdentity() throws Exception {
        setIdentityProperties();
        saveAndWait();

        assertThat(identity.getPhone(), equalTo(PHONE));
        assertThat(identity.getEmail(), equalTo(EMAIL));
        assertThat(identity.getAlias(), equalTo(ALIAS));
        assertThat(identity.isUploadEnabled(), equalTo(false));
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
        saveAndWait();

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

        waitUntil(() -> controller.alert != null && controller.alert.getAlert().isShowing());
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

    @Test
    public void hidesSpinnerByDefault() throws Exception {
        assertFalse(controller.uploadProgress.isVisible());
        assertTrue(controller.saveIdentity.isVisible());
    }

    @Test
    public void showsSpinnerOnUpload() throws Exception {
        controller.onFinish(lock::acquireUninterruptibly);

        new Thread(controller::saveIdentity).start();
        waitUntil(controller.uploadProgress::isVisible);
        assertFalse(controller.saveIdentity.isVisible());

        lock.release();
        waitUntil(() -> !controller.uploadProgress.isVisible());
        assertTrue(controller.saveIdentity.isVisible());
    }

    @Test
    public void uploadsIdentityIfEnabled() throws Exception {
        saveAndWait();
        verify(indexService).updateIdentity(identity, null);
    }

    protected void saveAndWait() throws InterruptedException {
        controller.onFinish(lock::release);
        controller.saveIdentity();

        assertTrue(lock.tryAcquire(1, TimeUnit.SECONDS));
    }

    @Test
    public void removesIdentityOnPrivateSave() throws Exception {
        identity.setUploadEnabled(true);
        createController();

        controller.setPrivate(true);
        saveAndWait();

        verify(indexService, never()).updateIdentity(identity, null);
        verify(indexService).removeIdentity(identity);
    }

    @After
    @Override
    public void tearDown() throws Exception {
        try {
            lock.release(lock.getQueueLength());
        } catch (IllegalMonitorStateException ignored) {}
        super.tearDown();
    }
}
