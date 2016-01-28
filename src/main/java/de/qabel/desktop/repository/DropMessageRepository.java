package de.qabel.desktop.repository;

import de.qabel.core.config.Account;
import de.qabel.core.config.Contact;
import de.qabel.core.drop.DropMessage;
import de.qabel.desktop.repository.exception.EntityNotFoundExcepion;
import de.qabel.desktop.repository.exception.PersistenceException;
import de.qabel.desktop.ui.actionlog.PersitsDropMessage;

import java.util.List;

public interface DropMessageRepository {
	void addMessage(DropMessage dropMessage, Contact contact, boolean send) throws PersistenceException;
	List<PersitsDropMessage> loadConversation(Contact contact) throws PersistenceException;
}
