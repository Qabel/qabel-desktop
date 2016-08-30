package de.qabel.desktop.ui.accounting.identity;

import com.airhacks.afterburner.views.FXMLView;
import de.qabel.desktop.ui.AbstractGuiTest;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

public class IdentityEditGuiTest extends AbstractGuiTest<IdentityEditController> {

    protected IdentityEditViewPage page;

    static String ALIAS = "IEGUI ALIAS";
    static String EMAIL = "IEGUI EMAIL";
    static String PHONE = "IEGUI 01510518518";

    @Override
    protected FXMLView getView() {
        return new IdentityEditView(generateInjection("identity", identity));
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
    public void isHiddenOnStartup() {
        controller.hide();
        assertFalse(controller.isShowing());
    }

    @Test
    public void isVisibleOnShow() {
        controller.show();
        assertTrue(controller.isShowing());
    }

    @Test
    public void canFillInformationAndSave() {
        controller.show();

        page.enterAlias(ALIAS);
        page.enterEmail(EMAIL);
        page.enterPhone(PHONE);

        page.presSave();

        waitUntil(() -> identity.getAlias().equals(ALIAS));

        assertEquals(ALIAS, identity.getAlias());
        assertEquals(EMAIL, identity.getEmail());
        assertEquals(PHONE, identity.getPhone());
    }

 }
