package de.qabel.desktop.ui.actionlog;

import com.airhacks.afterburner.views.FXMLView;
import de.qabel.core.drop.DropMessage;
import de.qabel.desktop.ui.AbstractGuiTest;
import org.junit.Test;

import java.util.Date;
import java.util.List;

import static junit.framework.TestCase.assertEquals;


public class ActionlogGuiTest extends AbstractGuiTest<ActionlogController> {

	@Override
	protected FXMLView getView() {
		return new ActionlogView();
	}

	@Test
	public void testSendMessage() {
		waitUntil(() -> controller.textarea != null);
		clickOn("#textarea").write("Message");
		clickOn("#submit");
		List<DropMessage> list = controller.httpDropConnector.receive(controller.identity, new Date(0L));
		assertEquals(1, list.size());
	}
}
