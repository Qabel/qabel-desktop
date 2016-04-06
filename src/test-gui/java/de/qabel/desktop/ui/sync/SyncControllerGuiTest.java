package de.qabel.desktop.ui.sync;

import com.airhacks.afterburner.views.FXMLView;
import de.qabel.desktop.ui.AbstractGuiTest;
import de.qabel.desktop.ui.sync.setup.SyncSetupPage;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class SyncControllerGuiTest extends AbstractGuiTest<SyncController> {
    private SyncPage page;

    @Before
    @Override
    public void setUp() throws Exception {
        super.setUp();
        page = new SyncPage(baseFXRobot, robot, controller);
    }

    @Override
    protected FXMLView getView() {
        return new SyncView();
    }

    @Test
    public void addViewTriggersSyncView() {
        page.add();
        assertNotNull("addSync not created", controller.addStage);
        waitUntil(() -> controller.addStage.isShowing());
        robot.targetWindow(controller.addStage);
        clickOn("#cancel");
    }

    @Test
    public void syncSetupIntegration() {
        page.add();
        robot.targetWindow(controller.addStage);

        SyncSetupPage setup = new SyncSetupPage(baseFXRobot, robot, null);
        setup.enterName("new sync");
        setup.enterLocalPath("tmp");
        setup.enterRemotePath("/");
        setup.startSync();

        waitUntil(() -> !controller.addStage.isShowing());
        robot.targetWindow(scene);

        assertEquals(1, controller.syncItemNodes.size());
    }
}
