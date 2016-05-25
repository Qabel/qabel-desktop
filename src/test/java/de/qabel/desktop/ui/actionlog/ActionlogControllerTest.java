package de.qabel.desktop.ui.actionlog;

import de.qabel.core.config.Account;
import de.qabel.core.config.Contact;
import de.qabel.core.config.Identity;
import de.qabel.core.drop.DropMessage;
import de.qabel.desktop.daemon.drop.TextMessage;
import de.qabel.desktop.repository.DropMessageRepository;
import de.qabel.desktop.repository.inmemory.InMemoryDropMessageRepository;
import de.qabel.desktop.ui.AbstractControllerTest;
import de.qabel.desktop.ui.actionlog.item.MyActionlogItemController;
import de.qabel.desktop.ui.actionlog.item.MyActionlogItemView;
import org.junit.Before;
import org.junit.Test;

import java.util.*;

import static de.qabel.desktop.AsyncUtils.waitUntil;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;


public class ActionlogControllerTest extends AbstractControllerTest {
    ActionlogController controller;
    Identity i;
    ActionlogView view;
    Contact c;
    String text = "MessageString";
    DropMessage dm;
    InMemoryDropMessageRepository repo;

    @Test
    public void addMessageToActionlogTest() throws Exception {
        contactRepository.save((Contact) dm.getSender(), i);
        controller.addMessageToActionlog(dm);
        assertEquals(1, controller.messages.getChildren().size());
    }

    @Test
    public void addOwnMessageToActionlogTest() throws Exception {
        controller.addOwnMessageToActionlog(dm);
        assertEquals(1, controller.messages.getChildren().size());
    }

    @Test
    public void marksSeenMessages() throws Exception {
        controller.setContact(c);
        waitUntil(() -> controller.contact == c);
        PersistenceDropMessage message = new PersistenceDropMessage(dm, c, i, false, false);
        System.out.println("saving message " + message);
        dropMessageRepository.save(message);
        waitUntil(message::isSeen, 10000L); // is done in cascaded async calls => higher timeout
    }

    @Test
    public void switchBetweenIdentitesTest() throws Exception {
        clientConfiguration.selectIdentity(i);
        controller.sendDropMessage(c, "msg1");
        i = identityBuilderFactory.factory().withAlias("NewIdentity").build();
        c = new Contact(i.getAlias(), i.getDropUrls(), i.getEcPublicKey());

        clientConfiguration.selectIdentity(i);
        controller.sendDropMessage(c, "msg2");

        List<PersistenceDropMessage> lst = dropMessageRepository.loadConversation(c, i);

        assertEquals(1, lst.size());
        assertEquals("msg2", TextMessage.fromJson(lst.get(0).dropMessage.getDropPayload()).getText());
    }

    @Test
    public void refreshTime() throws Exception {
        controller.sleepTime = 1;
        controller.dateRefresher.interrupt();
        DropMessage d = new DropMessage(i, new TextMessage("payload").toJson(), "test");
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
        },
            () -> "dateString was not refreshed as expected. expected: '" + old + "'" +
                ", actual: '" + messagesController.getDateLabel().getText()
        );
    }

    @Override
    @Before
    public void setUp() throws Exception {
        repo = new InMemoryDropMessageRepository();
        dropMessageRepository = repo;
        super.setUp();
        i = identityBuilderFactory.factory().withAlias("TestAlias").build();
        c = new Contact(i.getAlias(), i.getDropUrls(), i.getEcPublicKey());
        createController(i);
        controller.setContact(c);
        controller = (ActionlogController) view.getPresenter();

        dm = new DropMessage(c, new TextMessage(text).toJson(), DropMessageRepository.PAYLOAD_TYPE_MESSAGE);
    }

    private void createController(Identity i) {
        view = new ActionlogView();
        clientConfiguration.selectIdentity(i);
        clientConfiguration.setAccount(new Account("Provider", "user", "auth"));
        controller = (ActionlogController) view.getPresenter();
    }
}
