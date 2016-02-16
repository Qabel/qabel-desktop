package de.qabel.desktop.repository;

import de.qabel.core.config.Contact;
import de.qabel.core.drop.DropMessage;
import de.qabel.desktop.repository.exception.PersistenceException;
import de.qabel.desktop.ui.actionlog.PersistenceDropMessage;

import java.util.List;
import java.util.Observer;
import java.util.function.Consumer;

public interface DropMessageRepository{
	String PAYLOAD_TYPE_MESSAGE = "box_message";
	String PAYLOAD_TYPE_SHARE_NOTIFICATION = "box_share_notification";

	void addMessage(DropMessage dropMessage, Contact contact, boolean send) throws PersistenceException;
	List<PersistenceDropMessage> loadConversation(Contact contact) throws PersistenceException;

	void addObserver(Observer o);
}
