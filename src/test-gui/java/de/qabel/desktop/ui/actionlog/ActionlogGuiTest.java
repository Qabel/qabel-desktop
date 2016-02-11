package de.qabel.desktop.ui.actionlog;

import com.airhacks.afterburner.views.FXMLView;
import de.qabel.core.config.Contact;
import de.qabel.core.config.Identity;
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
		String text = "Message";
		waitUntil(() -> controller.textarea != null);
		Identity i = controller.identity;
		controller.c = new Contact(i,i.getAlias(),i.getDropUrls(), i.getEcPublicKey());
		clickOn("#textarea").write(text);
		clickOn("#submit");
		List<DropMessage> list = controller.httpDropConnector.receive(controller.identity, new Date(0L));
		assertEquals(1, list.size());
		assertEquals(text, list.get(0).getDropPayload());
		assertEquals(new Date().getTime(), list.get(0).getCreationDate().getTime(), 100000);
		assertEquals(controller.identity.getId(), list.get(0).getSender().getId());
	}

	@Test
	public void testSendMessageWithoutContent() {
		waitUntil(() -> controller.textarea != null);
		clickOn("#submit");
		List<DropMessage> list = controller.httpDropConnector.receive(controller.identity, new Date(0L));
		assertEquals(0, list.size());
	}
}
