package de.qabel.desktop.ui.accounting.wizard;

import de.qabel.core.repository.exception.EntityNotFoundException;
import de.qabel.core.repository.exception.PersistenceException;
import de.qabel.desktop.ui.AbstractControllerTest;
import org.junit.Test;

import static junit.framework.TestCase.assertFalse;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

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
        assertEquals(controller.getCurrentStep().getChildren().get(0).getId(), "alias");
    }

    @Test
    public void goToNextSteps() throws Exception {
        controller = getController();
        controller.alias.setText("test");
        moveThrough4Steps();
    }

    @Test
    public void goBackToSteps() throws Exception {
        controller = getController();
        controller.alias.setText("test");
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
        controller.alias.setText("test");
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
        controller.alias.setText("");
        assertTrue(controller.next.isDisable());
    }

    @Test
    public void goToStep2(){
        controller = getController();
        controller.alias.setText("test");
        controller.next();
        assertEquals("step2", controller.getCurrentStep().getId());
        assertEquals(controller.getCurrentStep().getChildren().get(0).getId(), "email");
    }

    @Test
    public void goToStep3(){
        goToStep2();
        controller.next();
        assertEquals("step3", controller.getCurrentStep().getId());
        assertEquals(controller.getCurrentStep().getChildren().get(0).getId(), "phone");
    }

    @Test
    public void emailValidation(){
        goToStep2();
        controller.email.setText("test");
        assertFalse(controller.emailValidator.isValid(controller.email.getText()));
        controller.email.setText("test@test.de");
        assertTrue(controller.emailValidator.isValid(controller.email.getText()));
    }

    @Test
    public void testAddsIdentity() throws EntityNotFoundException, PersistenceException {
        controller = getController();
        controller.clientConfiguration.selectIdentity(null);
        controller.alias.setText("a new identity");
        controller.next();
        assertEquals("a new identity", controller.identity.getAlias());
    }

    @Test
    public void testAddsPhone() throws EntityNotFoundException, PersistenceException {
        goToStep3();
        controller.phone.setText("+17626262626");
        controller.phoneValidator(controller.phone.getText());
        assertEquals("+17626262626", controller.identity.getPhone());
    }

    @Test
    public void testEmptyPhone() throws EntityNotFoundException, PersistenceException {
        goToStep3();
        controller.phone.setText("+5656");
        controller.phoneValidator(controller.phone.getText());
        assertEquals("", controller.identity.getPhone());
    }

    @Test
    public void testAddsEmail() throws EntityNotFoundException, PersistenceException {
        goToStep2();
        controller.email.setText("test@test.de");
        assertEquals("test@test.de", controller.identity.getEmail());
    }

    @Test
    public void testEditAlias() throws EntityNotFoundException, PersistenceException {
        goToStep2();
        controller.back();
        assertEquals("step1", controller.getCurrentStep().getId());
        controller.alias.setText("alias2");
        controller.next();
        assertEquals("alias2", controller.identity.getAlias());
    }
}
