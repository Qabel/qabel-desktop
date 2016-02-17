package de.qabel.desktop.repository;

import de.qabel.core.config.Contact;
import de.qabel.core.config.Entity;
import de.qabel.core.config.Identity;
import de.qabel.core.drop.DropMessage;
import de.qabel.desktop.repository.exception.PersistenceException;
import de.qabel.desktop.ui.actionlog.PersistenceDropMessage;

import java.util.List;
import java.util.Observer;
import java.util.function.Consumer;

public interface DropMessageRepository{
	String PAYLOAD_TYPE_MESSAGE = "box_message";
	String PAYLOAD_TYPE_SHARE_NOTIFICATION = "box_share_notification";

	void addMessage(DropMessage dropMessage, Entity from, Entity to, boolean send) throws PersistenceException;
	List<PersistenceDropMessage> loadConversation(Contact contact, Identity identity) throws PersistenceException;
	void addObserver(Observer o);
	List<PersistenceDropMessage> loadNewMessagesFromConversation(List<PersistenceDropMessage> dropMessages, Contact c, Identity identity);
}
