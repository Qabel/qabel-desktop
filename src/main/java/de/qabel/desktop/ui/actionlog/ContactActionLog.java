package de.qabel.desktop.ui.actionlog;

import de.qabel.core.config.Contact;
import de.qabel.desktop.repository.DropMessageRepository;

public class ContactActionLog extends Actionlog {
	public ContactActionLog(Contact contact, DropMessageRepository dropMessageRepository) {
		super(dropMessageRepository);
		String keyIdentifier = contact.getKeyIdentifier();
		addFilter(message -> keyIdentifier.equals(message.getSender().getKeyIdentifier())
				|| keyIdentifier.equals(message.getReceiver().getKeyIdentifier()));
	}
}
