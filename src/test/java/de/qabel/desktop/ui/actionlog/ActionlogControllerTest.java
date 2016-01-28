package de.qabel.desktop.ui.actionlog;

import de.qabel.core.config.Account;
import de.qabel.core.config.Contact;
import de.qabel.core.config.Identity;
import de.qabel.core.drop.DropMessage;
import de.qabel.core.drop.DropURL;
import de.qabel.core.exceptions.*;
import de.qabel.desktop.repository.exception.PersistenceException;
import de.qabel.desktop.ui.AbstractControllerTest;
import javafx.scene.layout.VBox;
import org.junit.Before;
import org.junit.Test;

import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;


public class ActionlogControllerTest extends AbstractControllerTest {

	ActionlogController controller;
	Identity i;
	ActionlogView view;
	Contact c;
	String fakeURL;
	String workingURL;
	String text = "MessageString";

	@Before
	public void setUp() throws Exception {
		super.setUp();
		fakeURL = "http://localhost:12345/abcdefghijklmnopqrstuvwxyzabcdefgworkingUrl";
		workingURL = "https://qdrop.prae.me/abcdefghijklmnopqrstuvwxyzabcdefgworkingUrl";
	}

	private void createController(Identity i, Contact c) {

		view = new ActionlogView();
		clientConfiguration.selectIdentity(i);
		clientConfiguration.setAccount(new Account("Provider", "user", "auth"));
		controller = (ActionlogController) view.getPresenter();
	}

	@Test
	public void sendDropMessageTest() throws QblDropPayloadSizeException, PersistenceException {
		i = identityBuilderFactory.factory().withAlias("TestAlias").build();
		c = new Contact(i, i.getAlias(), i.getDropUrls(), i.getEcPublicKey());
		createController(i, c);

		DropMessage dm = controller.sendDropMessage(c, text);
		assertEquals(text, dm.getDropPayload());
	}

	@Test
	public void sendDropMessageFailTest() throws QblDropPayloadSizeException, URISyntaxException, QblDropInvalidURL, PersistenceException {
		Collection<DropURL> collection = new ArrayList<>();
		DropURL drpoUrl = new DropURL(fakeURL);
		collection.add(drpoUrl);
		i = identityBuilderFactory.factory().withAlias("TestAlias").build();
		c = new Contact(i, i.getAlias(), collection, i.getEcPublicKey());
		createController(i, c);

		DropMessage dm = controller.sendDropMessage(c, text);
		assertNull(dm);
	}

	@Test
	public void receiveMessagesTest() throws QblDropPayloadSizeException, QblDropInvalidMessageSizeException, QblVersionMismatchException, QblSpoofedSenderException, URISyntaxException, QblDropInvalidURL, PersistenceException {
		String text = "MessageString";
		Date sinceDate = new Date(0L);
		Collection<DropURL> collection = new ArrayList<>();
		DropURL drpoUrl = new DropURL(workingURL);
		collection.add(drpoUrl);

		i = identityBuilderFactory.factory().withAlias("TestAlias").build();
		Identity identity = new Identity("TestAlias", collection, i.getPrimaryKeyPair());
		c = new Contact(identity, identity.getAlias(), collection, identity.getEcPublicKey());

		createController(identity, c);
		clientConfiguration.selectIdentity(identity);
		controller = (ActionlogController) view.getPresenter();
		controller.sendDropMessage(c, text);
		List<DropMessage> messages = controller.getDropMassages(sinceDate);

		assertEquals(1, messages.size());
		assertEquals(text, messages.get(0).getDropPayload());
	}

	@Test
	public void addMessageToActionlogTest() throws QblDropPayloadSizeException, PersistenceException {
		i = identityBuilderFactory.factory().withAlias("TestAlias").build();
		c = new Contact(i, i.getAlias(), i.getDropUrls(), i.getEcPublicKey());
		createController(i, c);
		controller = (ActionlogController) view.getPresenter();
		DropMessage dm = controller.sendDropMessage(c, text);
		controller.addMessageToActionlog(dm);
		assertEquals(1, controller.messages.getChildren().size());
	}

	@Test
	public void addOwnMessageToActionlogTest() throws QblDropPayloadSizeException, PersistenceException {
		i = identityBuilderFactory.factory().withAlias("TestAlias").build();
		c = new Contact(i, i.getAlias(), i.getDropUrls(), i.getEcPublicKey());
		createController(i, c);
		controller = (ActionlogController) view.getPresenter();
		DropMessage dm = controller.sendDropMessage(c, text);
		controller.addOwnMessageToActionlog(dm);
		assertEquals(1, controller.messages.getChildren().size());
	}


}