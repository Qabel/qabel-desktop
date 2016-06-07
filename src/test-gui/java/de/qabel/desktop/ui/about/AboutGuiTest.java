package de.qabel.desktop.ui.about;

import com.airhacks.afterburner.views.FXMLView;
import de.qabel.desktop.ui.AbstractGuiTest;

import org.junit.Before;
import org.junit.Test;


public class AboutGuiTest extends AbstractGuiTest<AboutController> {
    private AboutPage page;

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();
        page = new AboutPage(baseFXRobot, robot, controller);
    }

    @Override
    protected FXMLView getView() {
        return new AboutView();
    }

    @Test
    public void testThanksButton() {
        page.btnShowPopup();
    }
}
