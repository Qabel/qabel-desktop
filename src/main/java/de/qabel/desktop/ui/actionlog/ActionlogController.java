package de.qabel.desktop.ui.actionlog;

import de.qabel.core.config.Contact;
import de.qabel.core.config.Identity;
import de.qabel.core.drop.DropMessage;
import de.qabel.core.exceptions.*;
import de.qabel.desktop.config.ClientConfig;
import de.qabel.desktop.daemon.drop.TextMessage;
import de.qabel.desktop.repository.ContactRepository;
import de.qabel.desktop.repository.DropMessageRepository;
import de.qabel.desktop.repository.exception.EntityNotFoundExcepion;
import de.qabel.desktop.repository.exception.PersistenceException;
import de.qabel.desktop.ui.AbstractController;
import de.qabel.desktop.ui.actionlog.item.ActionlogItem;
import de.qabel.desktop.ui.actionlog.item.ActionlogItemView;
import de.qabel.desktop.ui.actionlog.item.MyActionlogItemView;
import de.qabel.desktop.ui.actionlog.item.OtherActionlogItemView;
import de.qabel.desktop.ui.connector.DropConnector;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextArea;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.net.URL;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static java.lang.Thread.*;


public class ActionlogController extends AbstractController implements Initializable, Observer {
    private static final ExecutorService executor = Executors.newSingleThreadExecutor();
    private static final Logger logger = LoggerFactory.getLogger(ActionlogController.class.getSimpleName());

    int sleepTime = 60000;
    List<ActionlogItemView> messageView = new LinkedList<>();

    @FXML
    VBox messages;

    @FXML
    TextArea textarea;

    @FXML
    ScrollPane scroller;

    @Inject
    ClientConfig clientConfiguration;

    @Inject
    private ContactRepository contactRepository;
    @Inject
    private DropMessageRepository dropMessageRepository;
    @Inject
    DropConnector dropConnector;

    Identity identity;
    Contact contact;
    List<PersistenceDropMessage> receivedDropMessages;
    List<ActionlogItem> messageControllers = new LinkedList<>();
    Thread dateRefresher;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        startDateRefresher();
        identity = clientConfiguration.getSelectedIdentity();
        dropMessageRepository.addObserver(this);
        clientConfiguration.onSelectIdentity(identity -> this.identity = identity);
        addListener();
    }

    private void startDateRefresher() {
        dateRefresher = new Thread(() -> {
            while (true) {
                messageControllers.forEach(ActionlogItem::refreshDate);
                try {
                    sleep(sleepTime);
                } catch (InterruptedException ignored) {
                }
            }
        });
        dateRefresher.start();
    }

    private void addListener() {

        textarea.setOnKeyPressed(keyEvent -> {
            if (keyEvent.getCode().equals(KeyCode.ENTER) && !keyEvent.isShiftDown()) {
                try {
                    submit();
                    keyEvent.consume();
                } catch (QblException | PersistenceException | EntityNotFoundExcepion e) {
                    alert(e);
                }
            } else if (keyEvent.getCode().equals(KeyCode.ENTER) && keyEvent.isShiftDown()) {
                keyEvent.consume();
                textarea.appendText("\n");
            }
        });

        ((Region) scroller.getContent()).heightProperty().addListener((ov, old_val, new_val) -> {
            if (scroller.getVvalue() != scroller.getVmax()) {
                scroller.setVvalue(scroller.getVmax());
            }
        });
    }

    protected void submit() throws QblDropPayloadSizeException, EntityNotFoundExcepion, PersistenceException, QblDropInvalidMessageSizeException, QblVersionMismatchException, QblSpoofedSenderException, QblNetworkInvalidResponseException {
        if (textarea.getText().equals("") || contact == null) {
            return;
        }
        sendDropMessage(contact, textarea.getText());
        textarea.setText("");
    }

    void sendDropMessage(Contact c, String text) throws QblDropPayloadSizeException, QblNetworkInvalidResponseException, PersistenceException {
        DropMessage d = new DropMessage(identity, new TextMessage(text).toJson(), DropMessageRepository.PAYLOAD_TYPE_MESSAGE);
        dropConnector.send(c, d);
        dropMessageRepository.addMessage(d, identity, c, true);
    }

    void loadMessages(Contact c) throws EntityNotFoundExcepion {
        try {
            if (receivedDropMessages == null) {
                Platform.runLater(messages.getChildren()::clear);
                receivedDropMessages = dropMessageRepository.loadConversation(c, identity);
                Platform.runLater(() -> addMessagesToView(receivedDropMessages));
            } else {
                List<PersistenceDropMessage> newMessages = dropMessageRepository.loadNewMessagesFromConversation(receivedDropMessages, c, identity);
                addNewMessagesToReceivedDropMessages(newMessages);
                addMessagesToView(newMessages);
            }

        } catch (PersistenceException e) {
            alert("Failed to load messages", e);
        }
    }

    private void addNewMessagesToReceivedDropMessages(List<PersistenceDropMessage> newMessages) {
        for (PersistenceDropMessage d : newMessages) {
            if (!d.isSeen()) {
                d.setSeen(true);
            }
            receivedDropMessages.add(d);
        }
    }

    private void addMessagesToView(List<PersistenceDropMessage> dropMessages) {
        Platform.runLater(() -> {
            for (PersistenceDropMessage d : dropMessages) {
                if (!d.isSeen()) {
                    d.setSeen(true);
                }
                if (d.isSent()) {
                    addOwnMessageToActionlog(d.getDropMessage());
                } else {
                    try {
                        addMessageToActionlog(d.getDropMessage());
                    } catch (EntityNotFoundExcepion e) {
                        logger.warn("failed to show message: " + e.getMessage(), e);
                    }
                }
            }
        });
    }

    void addMessageToActionlog(DropMessage dropMessage) throws EntityNotFoundExcepion {
        Map<String, Object> injectionContext = new HashMap<>();
        String senderKeyId = dropMessage.getSenderKeyId();
        if (senderKeyId == null) {
            senderKeyId = dropMessage.getSender().getKeyIdentifier();
        }
        Contact sender = contactRepository.findByKeyId(identity, senderKeyId);

        if(sender == null){
            sender = contactRepository.findByKeyId(identity, dropMessage.getSender().getKeyIdentifier());
        }
        injectionContext.put("dropMessage", dropMessage);
        injectionContext.put("contact", sender);
        OtherActionlogItemView otherItemView = new OtherActionlogItemView(injectionContext::get);
        messages.getChildren().add(otherItemView.getView());
        messageView.add(otherItemView);
        messageControllers.add((ActionlogItem) otherItemView.getPresenter());
    }

    void addOwnMessageToActionlog(DropMessage dropMessage) {

        if (dropMessage.getDropPayload().equals("")) {
            return;
        }
        Map<String, Object> injectionContext = new HashMap<>();
        injectionContext.put("dropMessage", dropMessage);
        MyActionlogItemView myItemView = new MyActionlogItemView(injectionContext::get);
        messages.getChildren().add(myItemView.getView());
        messageView.add(myItemView);
        messageControllers.add((ActionlogItem) myItemView.getPresenter());

    }

    void setText(String text) {
        textarea.setText(text);
    }

    @Override
    public void update(Observable o, Object arg) {
        if (o instanceof DropMessageRepository) {
            Platform.runLater(() -> {
                try {
                    loadMessages(contact);
                } catch (EntityNotFoundExcepion e) {
                    alert(e);
                }
            });
        }
    }

    public void setContact(Contact contact) {
        executor.submit(() -> {
            try {
                receivedDropMessages = null;
                this.contact = contact;
                loadMessages(this.contact);
            } catch (Exception e) {
                alert(e);
            }
        });

    }
}
