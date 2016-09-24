package de.qabel.desktop.ui.accounting;

import com.airhacks.afterburner.views.FXMLView;
import de.qabel.core.repository.exception.EntityNotFoundException;
import de.qabel.core.repository.exception.PersistenceException;
import de.qabel.core.repository.inmemory.InMemoryIdentityRepository;
import de.qabel.desktop.ui.AbstractGuiTest;
import org.junit.Before;
import org.junit.Test;

import static de.qabel.desktop.AsyncUtils.assertAsync;

public class AccountingGuiTest extends AbstractGuiTest<AccountingController> {
    private AccountingPage page;

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();
        ((InMemoryIdentityRepository)identityRepository).clear();
        page = new AccountingPage(baseFXRobot, robot, controller);
    }

    @Override
    protected FXMLView getView() {
        return new AccountingView();
    }

    @Test
    public void openWizard() throws EntityNotFoundException, PersistenceException {
        controller.layoutWindow = controller.identityList;
        page.open();
        assertAsync(() -> controller.wizardController.wizardPane.isVisible());
    }
}
