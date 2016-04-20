package de.qabel.desktop.ui.accounting;

import com.airhacks.afterburner.views.FXMLView;
import de.qabel.core.config.Identities;
import de.qabel.core.config.Identity;
import de.qabel.desktop.repository.exception.EntityNotFoundExcepion;
import de.qabel.desktop.repository.exception.PersistenceException;
import de.qabel.desktop.repository.inmemory.InMemoryIdentityRepository;
import de.qabel.desktop.ui.AbstractGuiTest;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

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
    public void testAddsIdentity() throws EntityNotFoundExcepion, PersistenceException {
        controller.clientConfiguration.selectIdentity(null);
        page.add().inputAndConfirm("a new identity");

        Identities identities = identityRepository.findAll();
        assertEquals(1, identities.getIdentities().size());
        Identity i = controller.clientConfiguration.getSelectedIdentity();
        assertEquals("a new identity", identities.getByKeyIdentifier(i.getKeyIdentifier()).getAlias());
    }
}
