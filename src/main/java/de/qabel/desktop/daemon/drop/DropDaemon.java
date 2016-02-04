package de.qabel.desktop.daemon.drop;


import de.qabel.core.config.Contact;
import de.qabel.core.config.Identity;
import de.qabel.core.drop.DropMessage;
import de.qabel.desktop.config.ClientConfiguration;
import de.qabel.desktop.repository.ContactRepository;
import de.qabel.desktop.repository.DropMessageRepository;
import de.qabel.desktop.repository.exception.EntityNotFoundExcepion;
import de.qabel.desktop.repository.exception.PersistenceException;
import de.qabel.desktop.ui.connector.Connector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Date;


public class DropDaemon implements Runnable {

	private ClientConfiguration config;
	private Date lastDate = null;
	private Connector httpDropConnector;
	private ContactRepository contactRepository;
	private DropMessageRepository dropMessageRepository;
	private long sleepTime = 10000L;
	private static final Logger logger = LoggerFactory.getLogger(DropDaemon.class.getSimpleName());

	public DropDaemon(ClientConfiguration config,
					  Connector httpDropConnector,
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
				}
				Thread.sleep(sleepTime);
			} catch (InterruptedException e) {
				logger.error("Thread stopped " + e.getMessage(), e);
				break;
			}
		}
	}

	void receiveMessages() throws PersistenceException {
		Identity identity = config.getSelectedIdentity();
		java.util.List<DropMessage> dropMessages = httpDropConnector.receive(identity, lastDate);

		for (DropMessage d : dropMessages) {
			Contact contact = null;
			try {
				contact = contactRepository.findByKeyId(identity, d.getSenderKeyId());
			} catch (EntityNotFoundExcepion e) {
				logger.error("Contact: with ID: " + d.getSenderKeyId() + " not found " + e.getMessage(), e);
				continue;
			}
			lastDate = config.getLastDropPoll(identity);
			if (lastDate.getTime() < d.getCreationDate().getTime()) {
				dropMessageRepository.addMessage(d, contact, false);
				config.setLastDropPoll(identity, d.getCreationDate());
			}
		}
	}
}
