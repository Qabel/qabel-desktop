package de.qabel.desktop.ui.actionlog;

import de.qabel.core.config.Account;
import de.qabel.core.config.Contact;
import de.qabel.core.config.Identity;
import de.qabel.core.drop.DropMessage;
import de.qabel.core.drop.DropURL;
import de.qabel.core.exceptions.*;
import de.qabel.desktop.repository.exception.EntityNotFoundExcepion;
import de.qabel.desktop.repository.exception.PersistenceException;
import de.qabel.desktop.ui.AbstractControllerTest;
import de.qabel.desktop.ui.actionlog.item.ActionlogItem;
import de.qabel.desktop.ui.actionlog.item.MyActionlogItemController;
import de.qabel.desktop.ui.actionlog.item.MyActionlogItemView;
import de.qabel.desktop.ui.actionlog.item.OtherActionlogItemView;
import de.qabel.desktop.ui.connector.HttpDropConnector;
import org.junit.Before;
import org.junit.Test;

import java.net.URISyntaxException;
import java.util.*;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;


public class ActionlogControllerTest extends AbstractControllerTest {

	ActionlogController controller;
	Identity i;
	ActionlogView view;
	Contact c;
	String text = "MessageString";

	@Test
	public void addMessageToActionlogTest() throws QblDropPayloadSizeException, PersistenceException, QblNetworkInvalidResponseException, EntityNotFoundExcepion {
		DropMessage dm = setup();
		controller.addMessageToActionlog(dm);
		assertEquals(1, controller.messages.getChildren().size());
	}

	@Test
	public void addOwnMessageToActionlogTest() throws QblDropPayloadSizeException, PersistenceException, QblNetworkInvalidResponseException {
		DropMessage dm = setup();
		controller.addOwnMessageToActionlog(dm);
		assertEquals(1, controller.messages.getChildren().size());
	}

	@Test
	public void switchBetweenIdentitesTest() throws QblDropPayloadSizeException, PersistenceException, QblNetworkInvalidResponseException, EntityNotFoundExcepion {
		setup();
		clientConfiguration.selectIdentity(i);
		controller.sendDropMessage(c, "msg1");
		i = identityBuilderFactory.factory().withAlias("NewIdentity").build();
		c = new Contact(i.getAlias(), i.getDropUrls(), i.getEcPublicKey());

		String msg2 = "msg2";
		controller.sendDropMessage(c, msg2);
		clientConfiguration.selectIdentity(i);

		List<PersistenceDropMessage> lst = dropMessageRepository.loadConversation(c, i);

		assertEquals(1, lst.size());
		assertEquals(msg2, lst.get(0).dropMessage.getDropPayload());
	}

	@Test
	public void refreshTime() throws EntityNotFoundExcepion {
		setup();
		controller.sleepTime = 1;
		controller.dateRefresher.interrupt();
		DropMessage d = new DropMessage(i,"payload", "test");
		Contact sender = new Contact(i.getAlias(), i.getDropUrls(), i.getEcPublicKey());

		Map<String, Object> injectionContext = new HashMap<>();
		injectionContext.put("dropMessage", d);
		injectionContext.put("contact", sender);

		MyActionlogItemView my = new MyActionlogItemView(injectionContext::get);
		MyActionlogItemController messagesController = (MyActionlogItemController) my.getPresenter();
		controller.messageControllers.add(messagesController);

		messagesController.setDropMessage(d);
		String old = messagesController.getDateLabel().getText();
		messagesController.getDateLabel().setText("");


		waitUntil(() -> {
			String newString = messagesController.getDateLabel().getText();
			return old.equals(newString);
		});
	}

	private DropMessage setup() {
		i = identityBuilderFactory.factory().withAlias("TestAlias").build();
		c = new Contact(i.getAlias(), i.getDropUrls(), i.getEcPublicKey());
		createController(i);
		controller = (ActionlogController) view.getPresenter();
		return new DropMessage(i, text, "dropMessage");
	}

	private void createController(Identity i) {
		view = new ActionlogView();
		clientConfiguration.selectIdentity(i);
		clientConfiguration.setAccount(new Account("Provider", "user", "auth"));
		controller = (ActionlogController) view.getPresenter();
	}
}