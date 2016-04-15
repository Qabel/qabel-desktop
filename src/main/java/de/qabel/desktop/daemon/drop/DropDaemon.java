package de.qabel.desktop.daemon.drop;


import de.qabel.core.config.Contact;
import de.qabel.core.config.Identity;
import de.qabel.core.drop.DropMessage;
import de.qabel.desktop.config.ClientConfig;
import de.qabel.desktop.repository.ContactRepository;
import de.qabel.desktop.repository.DropMessageRepository;
import de.qabel.desktop.repository.exception.EntityNotFoundExcepion;
import de.qabel.desktop.repository.exception.PersistenceException;
import de.qabel.desktop.ui.connector.DropConnector;
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
    private static final Logger logger = LoggerFactory.getLogger(DropDaemon.class.getSimpleName());

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
                } catch (EntityNotFoundExcepion entityNotFoundExcepion) {
                    entityNotFoundExcepion.printStackTrace();
                }
                Thread.sleep(sleepTime);
            } catch (InterruptedException e) {
                logger.error("Thread stopped " + e.getMessage(), e);
                break;
            }
        }
    }

    void receiveMessages() throws PersistenceException, EntityNotFoundExcepion {
        Identity identity = config.getSelectedIdentity();
        if (identity == null) {
            return;
        }
        List<DropMessage> dropMessages;
        try {
            dropMessages = httpDropConnector.receive(identity, lastDate);
        } catch (NullPointerException e) {
            return;
        }
        Contact sender;

        for (DropMessage d : dropMessages) {

            lastDate = config.getLastDropPoll(identity);
            if (lastDate.getTime() < d.getCreationDate().getTime()) {
                try {
                    sender = contactRepository.findByKeyId(identity, d.getSenderKeyId());
                } catch (EntityNotFoundExcepion e) {
                    logger.error("Contact: with ID: " + d.getSenderKeyId() + " not found " + e.getMessage(), e);
                    continue;
                }
                dropMessageRepository.addMessage(d, sender, identity, false);
                config.setLastDropPoll(identity, d.getCreationDate());
            }
        }
    }
}
