package de.qabel.desktop.ui.actionlog;

import com.airhacks.afterburner.views.FXMLView;
import de.qabel.desktop.ui.AbstractGuiTest;
import org.junit.Test;
import static junit.framework.TestCase.assertEquals;


public class ActionlogGuiTest extends AbstractGuiTest<ActionlogController> {

	@Override
	protected FXMLView getView() {
		return new ActionlogView();
	}

	@Test
	public void testSendMessage() {
		waitUntil(() -> controller.textarea != null);
		runLaterAndWait(() -> controller.textarea.setText("Test String"));
		clickOn("#submit");
		assertEquals(2, controller.messages.getChildren().size());
	}
}
