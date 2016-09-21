package de.qabel.desktop.ui.accounting.identity;

import com.airhacks.afterburner.views.FXMLView;
import de.qabel.desktop.ui.AbstractGuiTest;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

public class IdentityEditGuiTest extends AbstractGuiTest<IdentityEditController> {

    protected IdentityEditViewPage page;

    private static String ALIAS = "IEGUI ALIAS";
    private static String EMAIL = "IEGUI EMAIL";
    private static String PHONE = "IEGUI 01510518518";

    @Override
    protected FXMLView getView() {
        return new IdentityEditView(identity);
    }

    @Override
    public IdentityEditController getController() {
        return (IdentityEditController) getView().getPresenter();
    }

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();
        page = new IdentityEditViewPage(baseFXRobot, robot, controller);
    }

    @Test
    public void canFillInformationAndSave() {
        page.clearFields();

        page.enterAlias(ALIAS);
        page.enterEmail(EMAIL);
        page.enterPhone(PHONE);

        page.pressSave();

        waitUntil(() -> identity.getAlias().equals(ALIAS));

        assertEquals(ALIAS, identity.getAlias());
        assertEquals(EMAIL, identity.getEmail());
        assertEquals(PHONE, identity.getPhone());
    }
 }
