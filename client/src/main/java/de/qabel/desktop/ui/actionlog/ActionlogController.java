package de.qabel.desktop.ui.actionlog;

import com.airhacks.afterburner.views.QabelFXMLView;
import com.vdurmont.emoji.EmojiParser;
import de.qabel.core.config.Contact;
import de.qabel.core.config.Entity;
import de.qabel.core.config.Identity;
import de.qabel.core.drop.DropMessage;
import de.qabel.core.drop.DropMessageMetadata;
import de.qabel.core.exceptions.*;
import de.qabel.core.repository.ContactRepository;
import de.qabel.core.repository.exception.EntityNotFoundException;
import de.qabel.core.repository.exception.PersistenceException;
import de.qabel.desktop.config.ClientConfig;
import de.qabel.desktop.daemon.drop.TextMessage;
import de.qabel.desktop.repository.DropMessageRepository;
import de.qabel.desktop.ui.AbstractController;
import de.qabel.desktop.ui.actionlog.emoji.EmojiSelector;
import de.qabel.desktop.ui.actionlog.item.ActionlogItem;
import de.qabel.desktop.ui.actionlog.item.ActionlogItemView;
import de.qabel.desktop.ui.connector.DropConnector;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextArea;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import org.controlsfx.control.NotificationPane;
import org.controlsfx.control.PopOver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rx.Subscription;
import rx.schedulers.JavaFxScheduler;

import javax.inject.Inject;
import java.net.URL;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static java.lang.Thread.sleep;


public class ActionlogController extends AbstractController implements Initializable, Observer {
    private static final ExecutorService executor = Executors.newSingleThreadExecutor();
    private static final Logger logger = LoggerFactory.getLogger(ActionlogController.class);

    private ExecutorService messageLoadingExecutor = executor;

    @FXML
    public BorderPane chat;
    @FXML
    public ImageView emojiSelector;
    @FXML
    Button accept;

    @FXML
    Button ignore;

    int sleepTime = 60000;
    List<ActionlogItemView> messageView = new LinkedList<>();

    @FXML
    NotificationPane notification;

    @FXML
    Label notifcationMessage;

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
    private final List<PersistenceDropMessage> receivedDropMessages = new LinkedList<>();
    List<ActionlogItem> messageControllers = new LinkedList<>();
    Thread dateRefresher;
    private Set<PersistenceDropMessage> knownMessages = new HashSet<>();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        startDateRefresher();
        identity = clientConfiguration.getSelectedIdentity();
        dropMessageRepository.addObserver(this);
        clientConfiguration.onSelectIdentity(identity -> this.identity = identity);
        addListener();
        contactRepository.attach(this::updateNotification);
        scroller.setFitToWidth(true);
    }

    private void updateNotification() {
        notification.setManaged(contact.getStatus() == Contact.ContactStatus.UNKNOWN);
    }

    public List<PersistenceDropMessage> getReceivedDropMessages() {
        return receivedDropMessages;
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
                } catch (QblException | PersistenceException | EntityNotFoundException e) {
                    alert(e);
                }
            } else if (keyEvent.getCode().equals(KeyCode.ENTER) && keyEvent.isShiftDown()) {
                keyEvent.consume();
                textarea.appendText("\n");
            }
        });

        ((Region) scroller.getContent()).heightProperty().addListener(o -> {
            if (scroller.getVvalue() != scroller.getVmax()) {
                scroller.setVvalue(scroller.getVmax());
            }
        });
    }

    protected void submit() throws QblDropPayloadSizeException, EntityNotFoundException, PersistenceException, QblDropInvalidMessageSizeException, QblVersionMismatchException, QblSpoofedSenderException, QblNetworkInvalidResponseException {
        if (textarea.getText().equals("") || contact == null) {
            return;
        }
        sendDropMessage(contact, textarea.getText());
        textarea.setText("");
    }

    void sendDropMessage(Contact c, String text) throws QblDropPayloadSizeException, QblNetworkInvalidResponseException, PersistenceException {
        text = EmojiParser.parseToUnicode(text);
        DropMessage d = new DropMessage(identity, new TextMessage(text).toJson(), DropMessageRepository.PAYLOAD_TYPE_MESSAGE);
        d.setDropMessageMetadata(new DropMessageMetadata(identity));
        dropConnector.send(c, d);
        dropMessageRepository.addMessage(d, identity, c, true);
    }

    private void loadMessages(Contact c) throws EntityNotFoundException {
        try {
            if (receivedDropMessages.isEmpty()) {
                Platform.runLater(messages.getChildren()::clear);
                addMessagesToView(dropMessageRepository.loadConversation(c, identity));
            } else {
                addMessagesToView(dropMessageRepository.loadNewMessagesFromConversation(receivedDropMessages, c, identity));
            }

        } catch (PersistenceException e) {
            alert("Failed to load messages", e);
        }
    }

    private void insert(String text) {
        textarea.insertText(textarea.getCaretPosition(), text);
    }

    private void addMessagesToView(List<PersistenceDropMessage> dropMessages) {
        for (PersistenceDropMessage d : dropMessages) {
            Platform.runLater(() -> {
                try {
                    addMessage(d);
                } catch (EntityNotFoundException e) {
                    logger.warn("failed to show message: " + e.getMessage(), e);
                }
            });
        }
    }

    private void markSeen(PersistenceDropMessage d) {
        if (!d.isSeen() && chat.isVisible()) {
            d.setSeen(true);
            executor.submit(() -> {
                try {
                    dropMessageRepository.save(d);
                } catch (PersistenceException e) {
                    logger.error("failed to mark message seen", e);
                }
            });
        }
    }

    private Entity lastSender;
    void addReceivedMessage(DropMessage dropMessage) throws EntityNotFoundException {
        Map<String, Object> injectionContext = new HashMap<>();
        String senderKeyId = dropMessage.getSenderKeyId();
        if (senderKeyId == null) {
            senderKeyId = dropMessage.getSender().getKeyIdentifier();
        }
        Contact sender = contactRepository.findByKeyId(identity, senderKeyId);

        if (sender == null) {
            sender = contactRepository.findByKeyId(identity, dropMessage.getSender().getKeyIdentifier());
        }
        boolean first = lastSender != sender;
        lastSender = sender;
        injectionContext.put("dropMessage", dropMessage);
        injectionContext.put("sender", sender.getAlias());
        ActionlogItemView otherItemView = new ActionlogItemView(injectionContext::get);
        Parent view = otherItemView.getView();
        view.getStyleClass().add("sent");
        view.getStyleClass().add(first ? "first" : "sequence");
        messages.getChildren().add(view);
        messageView.add(otherItemView);
        messageControllers.add((ActionlogItem) otherItemView.getPresenter());
    }

    void addSentMessage(DropMessage dropMessage) throws EntityNotFoundException {

        if (dropMessage.getDropPayload().equals("")) {
            return;
        }
        Map<String, Object> injectionContext = new HashMap<>();
        injectionContext.put("dropMessage", dropMessage);
        injectionContext.put("sender", identity.getAlias());

        boolean first = lastSender != identity;
        lastSender = identity;
        ActionlogItemView myItemView = new ActionlogItemView(injectionContext::get);

        Parent view = myItemView.getView();
        view.getStyleClass().add("received");
        view.getStyleClass().add(first ? "first" : "sequence");
        messages.getChildren().add(view);
        messageView.add(myItemView);
        messageControllers.add((ActionlogItem) myItemView.getPresenter());
    }

    void setText(String text) {
        textarea.setText(text);
    }

    @Override
    public synchronized void update(Observable o, Object arg) {
        if (!(arg instanceof PersistenceDropMessage))
            return;
        PersistenceDropMessage message = (PersistenceDropMessage)arg;

        if (knownMessages.contains(message)) {
            return;
        }
        knownMessages.add(message);

        if (contact == null) {
            throw new IllegalStateException("cannot load messages without contact");
        }
        if (messageIsFromAnotherConversation(message)) {
            return;
        }

        Platform.runLater(() -> tryOrAlert(() -> addMessage(message)));
    }

    private boolean messageIsFromAnotherConversation(PersistenceDropMessage message) {
        return !(message.isSent() && contact == message.getReceiver() || !message.isSent() && contact == message.getSender());
    }

    protected void addMessage(PersistenceDropMessage message) throws EntityNotFoundException {
        knownMessages.add(message);
        receivedDropMessages.add(message);
        markSeen(message);
        if (message.getSender() == identity) {
            addSentMessage(message.getDropMessage());
        } else {
            addReceivedMessage(message.getDropMessage());
        }
    }

    public synchronized void setContact(Contact contact) {
        receivedDropMessages.clear();
        knownMessages.clear();
        this.contact = contact;
        lastSender = null;
        messageLoadingExecutor.submit(() -> {
            try {
                updateNotification();
                loadMessages(this.contact);
            } catch (Exception e) {
                alert(e);
            }
        });

    }

    void setMessageLoadingExecutor(ExecutorService messageLoadingExecutor) {
        this.messageLoadingExecutor = messageLoadingExecutor;
    }

    public void handleAccept() {
        saveContact();
    }

    private void saveContact() {
        contact.setStatus(Contact.ContactStatus.NORMAL);
        tryOrAlert(() -> contactRepository.save(contact, identity));
    }

    public void handleIgnore() {
        contact.setIgnored(true);
        saveContact();
    }

    public void selectEmoji() {
        PopOver popOver = new PopOver();
        popOver.getStyleClass().add("emojiSelector");
        EmojiSelector emojiSelector = new EmojiSelector();
        emojiSelector.getStylesheets().add(QabelFXMLView.getGlobalStyleSheet());
        popOver.setArrowLocation(PopOver.ArrowLocation.BOTTOM_RIGHT);
        popOver.setContentNode(emojiSelector);
        popOver.show(this.emojiSelector);

        Subscription subscription = emojiSelector.onSelect()
            .subscribeOn(JavaFxScheduler.getInstance())
            .subscribe(emoji -> {
                insert(" :" + emoji.getAliases().get(0) + ": ");
                textarea.requestFocus();
                popOver.hide();
            });
        popOver.setOnCloseRequest(event -> subscription.unsubscribe());
    }
}
