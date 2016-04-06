package de.qabel.desktop.ui.sync.setup;

import com.airhacks.afterburner.views.FXMLView;
import de.qabel.desktop.ui.AbstractGuiTest;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class SyncSetupGuiTest extends AbstractGuiTest<SyncSetupController> {
    private SyncSetupPage page;

    @Before
    @Override
    public void setUp() throws Exception {
        super.setUp();
        page = new SyncSetupPage(baseFXRobot, robot, controller);
    }

    @Override
	protected FXMLView getView() {
		return new SyncSetupView();
	}

	@Test
	public void testInputReacts() {
		page.enterName("testname");
        page.enterLocalPath("localPath");
        page.enterRemotePath("remotePath");

		assertEquals("testname", controller.name.getText());
		assertEquals("localPath", controller.localPath.getText());
		assertEquals("remotePath", controller.remotePath.getText());
	}
}
