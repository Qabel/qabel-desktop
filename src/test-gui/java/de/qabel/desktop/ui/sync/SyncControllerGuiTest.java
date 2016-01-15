package de.qabel.desktop.ui.sync;

import com.airhacks.afterburner.views.FXMLView;
import de.qabel.desktop.ui.AbstractGuiTest;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class SyncControllerGuiTest extends AbstractGuiTest<SyncController> {
	@Override
	protected FXMLView getView() {
		return new SyncView();
	}

	@Test
	public void addViewTriggersSyncView() {
		clickOn("#addSync");
		assertNotNull("addSync not created", controller.addStage);
		waitUntil(() -> controller.addStage.isShowing());
		robot.target(controller.addStage);
		clickOn("#cancel");
	}

	@Test
	public void syncSetupIntegration() {
		clickOn("#addSync");
		waitUntil(() -> controller.addStage.isShowing());
		robot.target(controller.addStage);
		clickOn("#name").write("new sync");
		clickOn("#localPath").write("tmp");
		clickOn("#remotePath").write("/");
		clickOn("#start");
		waitUntil(() -> !controller.addStage.isShowing());
		robot.target(scene);

		assertEquals(1, controller.syncItemNodes.size());
	}
}
