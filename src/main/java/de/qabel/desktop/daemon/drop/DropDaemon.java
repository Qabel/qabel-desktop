package de.qabel.desktop.daemon.drop;


import de.qabel.core.config.Contact;
import de.qabel.core.config.Identity;
import de.qabel.core.drop.DropMessage;
import de.qabel.core.repository.ContactRepository;
import de.qabel.core.repository.IdentityRepository;
import de.qabel.core.repository.exception.EntityNotFoundException;
import de.qabel.core.repository.exception.PersistenceException;
import de.qabel.desktop.config.ClientConfig;
import de.qabel.desktop.repository.DropMessageRepository;
import de.qabel.desktop.ui.connector.DropConnector;
import de.qabel.desktop.ui.connector.DropPollResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Date;
import java.util.List;


public class DropDaemon implements Runnable {
    private ClientConfig config;
    private DropConnector httpDropConnector;
    private ContactRepository contactRepository;
    private DropMessageRepository dropMessageRepository;
    private IdentityRepository identityRepository;
    private long sleepTime = 10000L;
    private static final Logger logger = LoggerFactory.getLogger(DropDaemon.class);

    public DropDaemon(ClientConfig config,
                      DropConnector httpDropConnector,
                      ContactRepository contactRepository,
                      DropMessageRepository dropMessageRepository,
                      IdentityRepository identityRepository
    ) {
        this.config = config;
        this.httpDropConnector = httpDropConnector;
        this.contactRepository = contactRepository;
        this.dropMessageRepository = dropMessageRepository;
        this.identityRepository = identityRepository;
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
        for (Identity identity : identityRepository.findAll().getIdentities()) {
            try {
                receiveMessages(identity);
            } catch (Exception e) {
                logger.error("Unexpected error while polling drops: " + e.getMessage(), e);
            }
        }
    }

    private void receiveMessages(Identity identity) throws PersistenceException, EntityNotFoundException {
        try {
            Date lastDate = config.getLastDropPoll(identity);
            DropPollResponse response = httpDropConnector.receive(identity, new Date(lastDate.getTime() + 1000L));
            List<DropMessage> dropMessages = response.dropMessages;

            Contact sender;
            for (DropMessage d : dropMessages) {
                lastDate = config.getLastDropPoll(identity);
                if (lastDate.getTime() < d.getCreationDate().getTime()) {
                    try {
                        String senderKeyId = d.getSenderKeyId();
                        if (senderKeyId == null) {
                            senderKeyId = d.getSender().getKeyIdentifier();
                        }
                        sender = contactRepository.findByKeyId(identity, senderKeyId);
                    } catch (EntityNotFoundException e) {
                        logger.error("Contact: with ID: " + d.getSenderKeyId() + " not found " + e.getMessage(), e);
                        continue;
                    }
                    dropMessageRepository.addMessage(d, sender, identity, false);
                    logger.debug("setting response date to " + response.date);
                }
            }
            if (response.date != null) {
                config.setLastDropPoll(identity, response.date);
            }
        } catch (NullPointerException e) {
            logger.warn("failed to receive dropMessage " + e.getMessage(), e);
        }
    }

    void setSleepTime(long sleepTime) {
        this.sleepTime = sleepTime;
    }
}
