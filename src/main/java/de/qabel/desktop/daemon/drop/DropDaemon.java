package de.qabel.desktop.daemon.drop;


import de.qabel.core.config.Contact;
import de.qabel.core.config.Identity;
import de.qabel.core.drop.DropMessage;
import de.qabel.desktop.config.ClientConfig;
import de.qabel.desktop.repository.ContactRepository;
import de.qabel.desktop.repository.DropMessageRepository;
import de.qabel.desktop.repository.exception.EntityNotFoundException;
import de.qabel.desktop.repository.exception.PersistenceException;
import de.qabel.desktop.ui.connector.DropConnector;
import de.qabel.desktop.ui.connector.DropPollResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Date;
import java.util.List;


public class DropDaemon implements Runnable {

    private ClientConfig config;
    private Date lastDate;
    private DropConnector httpDropConnector;
    private ContactRepository contactRepository;
    private DropMessageRepository dropMessageRepository;
    private long sleepTime = 10000L;
    private static final Logger logger = LoggerFactory.getLogger(DropDaemon.class);

    public DropDaemon(ClientConfig config,
                      DropConnector httpDropConnector,
                      ContactRepository contactRepository,
                      DropMessageRepository dropMessageRepository
    ) {
        this.config = config;
        this.httpDropConnector = httpDropConnector;
        this.contactRepository = contactRepository;
        this.dropMessageRepository = dropMessageRepository;
    }

    @Override
    public void run() {
        while (!Thread.interrupted()) {
            try {
                try {
                    receiveMessages();
                } catch (PersistenceException e) {
                    logger.error("Persitence fail: " + e.getMessage(), e);
                    continue;
                } catch (EntityNotFoundException entityNotFoundException) {
                    entityNotFoundException.printStackTrace();
                }
                Thread.sleep(sleepTime);
            } catch (InterruptedException e) {
                logger.error("Thread stopped " + e.getMessage(), e);
                break;
            }
        }
    }

    void receiveMessages() throws PersistenceException, EntityNotFoundException {
        Identity identity = config.getSelectedIdentity();
        if (identity == null) {
            return;
        }

        try {
            lastDate = config.getLastDropPoll(identity);
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
                    System.out.println("setting response date to " + response.date);
                }
            }
            if (response.date != null) {
                config.setLastDropPoll(identity, response.date);
            }
        } catch (NullPointerException e) {
            logger.warn("failed to receive dropMessage " + e.getMessage(), e);
            return;
        }
    }
}
