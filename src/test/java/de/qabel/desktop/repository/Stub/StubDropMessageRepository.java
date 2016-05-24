package de.qabel.desktop.repository.Stub;

import de.qabel.core.config.Contact;
import de.qabel.core.config.Entity;
import de.qabel.core.config.Identity;
import de.qabel.core.drop.DropMessage;
import de.qabel.desktop.repository.DropMessageRepository;
import de.qabel.desktop.repository.exception.PersistenceException;
import de.qabel.desktop.ui.actionlog.PersistenceDropMessage;

import java.util.*;

public class StubDropMessageRepository extends Observable implements DropMessageRepository {
    public PersistenceDropMessage lastMessage;
    private List<PersistenceDropMessage> messages = new LinkedList<>();

    @Override
    public void addMessage(DropMessage dropMessage, Entity from, Entity to, boolean send) throws PersistenceException {
        PersistenceDropMessage pdm = new PersistenceDropMessage(dropMessage,from, to, send, send);
        save(pdm);
    }

    @Override
    public synchronized void save(PersistenceDropMessage pdm) throws PersistenceException {
        messages.add(pdm);
        setChanged();
        notifyObservers(pdm);
    }

    @Override
    public synchronized List<PersistenceDropMessage> loadConversation(Contact contact, Identity identity) throws PersistenceException {
        List<PersistenceDropMessage> result = new LinkedList<>();
        for (PersistenceDropMessage message : messages) {
            if (isPartOfConversation(message, identity, contact)) {
                result.add(message);
            }
        }
        return result;
    }

    private boolean isPartOfConversation(PersistenceDropMessage message, Identity identity, Contact contact) {
        String expectedKey1 = identity.getKeyIdentifier();
        String expectedKey2 = contact.getKeyIdentifier();
        String actualKey1 = message.getReceiver().getKeyIdentifier();
        String actualKey2 = message.getSender().getKeyIdentifier();

        return expectedKey1.equals(actualKey1) && expectedKey2.equals(actualKey2)
            || expectedKey2.equals(actualKey1) && expectedKey1.equals(actualKey2);
    }

    @Override
    public List<PersistenceDropMessage> loadNewMessagesFromConversation(List<PersistenceDropMessage> dropMessages, Contact c, Identity identity) {
        try {
            return loadConversation(c, identity);
        } catch (PersistenceException e) {
            throw new IllegalStateException("failed to load conversation: " + e.getMessage(), e);
        }
    }
}
