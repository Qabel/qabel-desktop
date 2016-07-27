package de.qabel.desktop.ui.feedback;

import com.airhacks.afterburner.views.FXMLView;
import de.qabel.desktop.ui.AbstractGuiTest;
import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class FeedbackGuiTest extends AbstractGuiTest<FeedbackController> {
    private FeedbackPage page;

    @Override
    public void setUp() throws Exception {
        super.setUp();
        page = new FeedbackPage(baseFXRobot, robot, controller);
    }

    @Override
    protected FXMLView getView() {
        return new FeedbackView();
    }

    @Test
    public void sendFeedbackShowsThanksDialog() {
        page.sendFeedback();
        assertTrue(controller.alert.isShowing());

        page.confirmAlertBox();
        assertFalse(controller.alert.isShowing());
    }
}
