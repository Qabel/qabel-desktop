package de.qabel.desktop.daemon.drop;


import de.qabel.core.config.Contact;
import de.qabel.core.config.Entity;
import de.qabel.core.config.Identity;
import de.qabel.core.drop.DropMessage;
import de.qabel.core.repository.ContactRepository;
import de.qabel.core.repository.entities.ChatDropMessage;
import de.qabel.core.repository.exception.EntityNotFoundException;
import de.qabel.core.repository.exception.PersistenceException;
import de.qabel.core.service.ChatService;
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
    private long sleepTime = 10000L;
    private static final Logger logger = LoggerFactory.getLogger(DropDaemon.class);

    public DropDaemon(ChatService chatService,
                      DropMessageRepository dropMessageRepository, ContactRepository contactRepository) {
        this.chatService = chatService;
        this.dropMessageRepository = dropMessageRepository;
        this.contactRepository = contactRepository;
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

    void receiveMessages() throws PersistenceException, EntityNotFoundException {
        try {
            Map<Identity, List<ChatDropMessage>> identityListMap = chatService.refreshMessages();
            for (Map.Entry<Identity, List<ChatDropMessage>> messages: identityListMap.entrySet()) {
                Identity identity = messages.getKey();
                for (ChatDropMessage msg: messages.getValue()) {
                    Contact contact = contactRepository.find(msg.getContactId());
                    DropMessage dropMessage = new DropMessage(contact, msg.getPayload().toString(), msg.getMessageType().getType());
                    Entity sender;
                    Entity receiver;
                    Boolean sent;
                    if (msg.getDirection().equals(ChatDropMessage.Direction.INCOMING)) {
                        sender = contact;
                        receiver = identity;
                        sent = false;
                    } else {
                        sender = identity;
                        receiver = contact;
                        sent = true;
                    }
                    PersistenceDropMessage message = new PersistenceDropMessage(dropMessage, sender, receiver, sent, false);
                    dropMessageRepository.save(message);
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
