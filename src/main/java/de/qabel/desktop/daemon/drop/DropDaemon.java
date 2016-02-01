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

import java.util.Date;


public class DropDaemon implements Runnable {

	private ClientConfiguration config;
	private Date lastDate = null;
	private Connector httpDropConnector;
	private ContactRepository contactRepository;
	private DropMessageRepository dropMessageRepository;
	private long sleepTime = 10000L;

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
		Identity identity = config.getSelectedIdentity();
		while (!Thread.interrupted()) {
			try {
				receiveMessages(identity);
				Thread.sleep(sleepTime);
			} catch (InterruptedException | PersistenceException e) {
				e.printStackTrace();
			}
		}
	}

	void receiveMessages(Identity identity) throws PersistenceException {
		java.util.List<DropMessage> dropMessages = httpDropConnector.receive(identity, lastDate);

		for (DropMessage d : dropMessages) {
			Contact contact = null;
			try {
				contact = contactRepository.findByKeyId(identity, d.getSenderKeyId());
			} catch (EntityNotFoundExcepion entityNotFoundExcepion) {
				entityNotFoundExcepion.printStackTrace();
			}
			lastDate = config.getLastUpdate();
			if (lastDate == null || lastDate.getTime() < d.getCreationDate().getTime()) {
				dropMessageRepository.addMessage(d, contact, false);
				config.setLastUpdate(d.getCreationDate());
			}
		}
	}
}
