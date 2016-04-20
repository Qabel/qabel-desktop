package de.qabel.desktop.ui.actionlog;

import de.qabel.core.config.Contact;
import de.qabel.core.config.Identity;
import de.qabel.desktop.repository.DropMessageRepository;
import de.qabel.desktop.repository.exception.PersistenceException;

import javax.inject.Inject;
import java.util.function.Function;

public class ContactActionLog extends Actionlog {
    private final Function<PersistenceDropMessage, Boolean> filter;

    public ContactActionLog(Identity identity, Contact contact, DropMessageRepository dropMessageRepository) {
        super(dropMessageRepository);
        String keyIdentifier = contact.getKeyIdentifier();
        filter = message -> keyIdentifier.equals(message.getSender().getKeyIdentifier())
            || keyIdentifier.equals(message.getReceiver().getKeyIdentifier());
        addFilter(filter);

        loadMessages(identity, contact, dropMessageRepository);
    }

    @Inject
    private void loadMessages(Identity identity, Contact contact, DropMessageRepository dropMessageRepository) {
        try {
            dropMessageRepository.loadConversation(contact, identity).forEach(this::handleMessage);
        } catch (PersistenceException e) {
            throw new IllegalStateException("failed to initialize actionlog: " + e.getMessage(), e);
        }
    }
}
