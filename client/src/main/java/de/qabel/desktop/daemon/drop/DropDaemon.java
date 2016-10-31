package de.qabel.desktop.daemon.drop;


import de.qabel.core.config.Contact;
import de.qabel.core.config.Entity;
import de.qabel.core.config.Identity;
import de.qabel.core.drop.DropMessage;
import de.qabel.core.event.EventSink;
import de.qabel.core.repository.ContactRepository;
import de.qabel.chat.repository.entities.ChatDropMessage;
import de.qabel.chat.service.ChatService;
import de.qabel.core.repository.IdentityRepository;
import de.qabel.desktop.repository.DropMessageRepository;
import de.qabel.desktop.ui.actionlog.PersistenceDropMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;


public class DropDaemon implements Runnable {
    private final ChatService chatService;
    private final DropMessageRepository dropMessageRepository;
    private final ContactRepository contactRepository;
    private final IdentityRepository identityRepository;
    private final EventSink events;
    private long sleepTime = 10000L;
    private static final Logger logger = LoggerFactory.getLogger(DropDaemon.class);

    public DropDaemon(ChatService chatService,
                      DropMessageRepository dropMessageRepository,
                      ContactRepository contactRepository,
                      IdentityRepository identityRepository,
                      EventSink events
    ) {
        this.chatService = chatService;
        this.dropMessageRepository = dropMessageRepository;
        this.contactRepository = contactRepository;
        this.identityRepository = identityRepository;
        this.events = events;
    }

    @Override
    public void run() {
        while (!Thread.interrupted()) {
            try {
                try {
                    receiveMessages();
                } catch (Exception e) {
                    logger.error("Unexpected error while polling drops: " + e.getMessage(), e);
                }
                Thread.sleep(sleepTime);
            } catch (InterruptedException e) {
                logger.error("Thread stopped " + e.getMessage(), e);
                break;
            }
        }
    }

    void receiveMessages() {
        try {
            Map<String, List<ChatDropMessage>> identityListMap = chatService.refreshMessages();
            for (Map.Entry<String, List<ChatDropMessage>> messages: identityListMap.entrySet()) {
                String identityKeyId = messages.getKey();
                Identity identity = identityRepository.find(identityKeyId);
                for (ChatDropMessage msg: messages.getValue()) {
                    Contact contact = contactRepository.find(msg.getContactId());
                    DropMessage dropMessage = new DropMessage(contact, msg.getPayload().toString(), msg.getMessageType().getType());
                    boolean incoming = msg.getDirection().equals(ChatDropMessage.Direction.INCOMING);
                    Entity sender = incoming ? contact : identity;
                    Entity receiver = incoming ? identity : contact;
                    PersistenceDropMessage message = new PersistenceDropMessage(dropMessage, sender, receiver, !incoming, false);
                    dropMessageRepository.save(message);
                    events.push(new MessageReceivedEvent(message));
                }
            }
        } catch (Exception e) {
            logger.error("Unexpected error while polling drops: " + e.getMessage(), e);
        }
    }

    void setSleepTime(long sleepTime) {
        this.sleepTime = sleepTime;
    }
}
