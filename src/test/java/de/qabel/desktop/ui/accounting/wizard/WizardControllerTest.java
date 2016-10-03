package de.qabel.desktop.ui.accounting.wizard;

import de.qabel.core.config.Identities;
import de.qabel.core.config.Identity;
import de.qabel.core.repository.exception.EntityNotFoundException;
import de.qabel.core.repository.exception.PersistenceException;
import de.qabel.desktop.ui.AbstractControllerTest;
import org.junit.Test;

import static de.qabel.desktop.AsyncUtils.assertAsync;
import static junit.framework.TestCase.assertFalse;
import static org.hamcrest.Matchers.emptyOrNullString;
import static org.junit.Assert.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;

public class WizardControllerTest extends AbstractControllerTest {

    private WizardController controller;

    private WizardController getController() {
        WizardView view = new WizardView();
        view.getView();
        return (WizardController) view.getPresenter();
    }



    @Test
    public void createController() throws Exception {
        controller = getController();
        assertNotNull(controller);
    }

    @Test
    public void loadFirstStep() throws Exception {
        controller = getController();
        assertEquals("step1", controller.getCurrentStep().getId());
        assertEquals(controller.getCurrentStep().getChildren().get(0).getId(), "aliasInput");
    }

    @Test
    public void goToNextSteps() throws Exception {
        controller = getController();
        controller.aliasInput.setText("test");
        moveThrough4Steps();
    }

    @Test
    public void goBackToSteps() throws Exception {
        controller = getController();
        controller.aliasInput.setText("test");
        moveThrough4Steps();
        back4Steps();
    }

    public void back4Steps() {
        assertEquals("step4", controller.getCurrentStep().getId());
        controller.back();
        assertEquals("step3", controller.getCurrentStep().getId());
        controller.back();
        assertEquals("step2", controller.getCurrentStep().getId());
        controller.back();
        assertEquals("step1", controller.getCurrentStep().getId());
    }

    @Test
    public void goToNextStepFailure() throws Exception {
        controller = getController();
        controller.aliasInput.setText("test");
        moveThrough4Steps();
    }

    public void moveThrough4Steps() {
        assertEquals("step1", controller.getCurrentStep().getId());
        controller.next();
        assertEquals("step2", controller.getCurrentStep().getId());
        controller.next();
        assertEquals("step3", controller.getCurrentStep().getId());
        controller.next();
        assertEquals("step4", controller.getCurrentStep().getId());
    }

    @Test
    public void emptyIdentityValidation(){
        controller = getController();
        controller.aliasInput.setText("");
        assertTrue(controller.nextButton.isDisable());
    }

    @Test
    public void goToStep2(){
        controller = getController();
        controller.aliasInput.setText("test");
        controller.next();
        assertEquals("step2", controller.getCurrentStep().getId());
        assertEquals(controller.getCurrentStep().getChildren().get(0).getId(), "emailInput");
    }

    @Test
    public void goToStep3(){
        goToStep2();
        controller.next();
        assertEquals("step3", controller.getCurrentStep().getId());
        assertEquals(controller.getCurrentStep().getChildren().get(0).getId(), "phoneInput");
    }

    @Test
    public void emailValidation(){
        goToStep2();
        controller.emailInput.setText("test");
        assertFalse(controller.emailValidator.isValid(controller.emailInput.getText()));
        controller.emailInput.setText("test@test.de");
        assertTrue(controller.emailValidator.isValid(controller.emailInput.getText()));
    }

    @Test
    public void testAddsIdentity() throws EntityNotFoundException, PersistenceException {
        controller = getController();
        controller.clientConfiguration.selectIdentity(null);
        Identities identities = identityRepository.findAll();
        assertEquals(1, identityRepository.findAll().getIdentities().size());
        controller.aliasInput.setText("a new identity");
        controller.next();
        controller.next();
        controller.next();
        controller.finishWizard();
        Identity selectedIdentity = controller.clientConfiguration.getSelectedIdentity();
        assertEquals(2, identityRepository.findAll().getIdentities().size());
        assertEquals("a new identity", identities.getByKeyIdentifier(selectedIdentity.getKeyIdentifier()).getAlias());
    }

    @Test
    public void testAddsPhone() throws EntityNotFoundException, PersistenceException {
        goToStep3();
        controller.phoneInput.setText("+17626262626");
        controller.validatePhone(controller.phoneInput.getText());
        controller.next();
        controller.finishWizard();
        Identities identities = identityRepository.findAll();
        Identity selectedIdentity = controller.clientConfiguration.getSelectedIdentity();
        assertEquals(2, identityRepository.findAll().getIdentities().size());
        assertEquals("+17626262626", identities.getByKeyIdentifier(selectedIdentity.getKeyIdentifier()).getPhone());

        assertTrue(selectedIdentity.isUploadEnabled());
        assertAsync(() -> verify(indexService).updateIdentity(selectedIdentity, null));
    }

    @Test
    public void testEmptyPhone() throws EntityNotFoundException, PersistenceException {
        goToStep3();
        controller.phoneInput.setText("+5656");
        controller.validatePhone(controller.phoneInput.getText());
        controller.finishWizard();
        Identities identities = identityRepository.findAll();
        Identity selectedIdentity = controller.clientConfiguration.getSelectedIdentity();
        assertThat(identities.getByKeyIdentifier(selectedIdentity.getKeyIdentifier()).getPhone(), emptyOrNullString());
    }

    @Test
    public void testAddsEmail() throws EntityNotFoundException, PersistenceException {
        goToStep2();
        controller.emailInput.setText("test@test.de");
        controller.finishWizard();
        Identities identities = identityRepository.findAll();
        Identity selectedIdentity = controller.clientConfiguration.getSelectedIdentity();
        assertEquals("test@test.de", identities.getByKeyIdentifier(selectedIdentity.getKeyIdentifier()).getEmail());

        assertTrue(selectedIdentity.isUploadEnabled());
        assertAsync(() -> verify(indexService).updateIdentity(selectedIdentity, null));
    }

    @Test
    public void testEditAliasInput() throws EntityNotFoundException, PersistenceException {
        goToStep2();
        controller.back();
        assertEquals("step1", controller.getCurrentStep().getId());
        controller.aliasInput.setText("alias2");
        controller.next();
        assertEquals("alias2", controller.identity.getAlias());
    }

    @Test
    public void testEmptyPhoneAndMailDoesNotGetUploaded() throws Exception {
        controller = getController();
        controller.aliasInput.setText("test");
        moveThrough4Steps();
        controller.finishButton.fire();

        Identity selectedIdentity = controller.clientConfiguration.getSelectedIdentity();
        assertFalse(selectedIdentity.isUploadEnabled());

        verifyZeroInteractions(indexService);
    }
}
