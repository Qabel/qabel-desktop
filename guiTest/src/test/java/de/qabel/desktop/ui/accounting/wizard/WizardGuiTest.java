package de.qabel.desktop.ui.accounting.wizard;

import com.airhacks.afterburner.views.FXMLView;
import de.qabel.core.config.Identities;
import de.qabel.core.config.Identity;
import de.qabel.desktop.ui.AbstractGuiTest;
import org.junit.Before;
import org.junit.Test;

import static de.qabel.desktop.AsyncUtils.assertAsync;
import static org.junit.Assert.assertEquals;


public class WizardGuiTest extends AbstractGuiTest<WizardController> {
    private WizardPage page;

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();
        page = new WizardPage(baseFXRobot, robot, controller);
    }

    @Override
    protected FXMLView getView() {
        return new WizardView();
    }

    @Test
    public void testAddIdentityWizard() throws Exception {
        page.showPopup();
        controller.clientConfiguration.selectIdentity(null);
        Identities identities = identityRepository.findAll();
        assertEquals(1, identityRepository.findAll().getIdentities().size());
        page.enterAlias("testGui");
        page.next();
        assertAsync(controller.emailInput::isVisible);
        page.next();
        assertAsync(controller.phoneInput::isVisible);
        page.next();
        assertAsync(controller.finishButton::isVisible);
        page.finish();
        waitUntil(() -> controller.clientConfiguration.getSelectedIdentity() != null);
        Identity selectedIdentity = controller.clientConfiguration.getSelectedIdentity();
        assertEquals(2, identityRepository.findAll().getIdentities().size());
        assertEquals("testGui", identities.getByKeyIdentifier(selectedIdentity.getKeyIdentifier()).getAlias());
    }
}
