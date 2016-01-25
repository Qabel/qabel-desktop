package de.qabel.desktop.ui.sync.setup;

import com.airhacks.afterburner.views.FXMLView;
import de.qabel.desktop.ui.AbstractGuiTest;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class SyncSetupGuiTest extends AbstractGuiTest<SyncSetupController> {

	@Override
	protected FXMLView getView() {
		return new SyncSetupView();
	}

	@Test
	public void testInputReacts() {
		clickOn("#name").write("testname");
		clickOn("#localPath").write("localPath");
		clickOn("#remotePath").write("remotePath");
		assertEquals("testname", controller.name.getText());
		assertEquals("localPath", controller.localPath.getText());
		assertEquals("remotePath", controller.remotePath.getText());
	}
}