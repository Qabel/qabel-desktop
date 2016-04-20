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
    private HashMap<String,List<PersistenceDropMessage>> messagesMap = new HashMap<>();


    @Override
    public void addMessage(DropMessage dropMessage, Entity from, Entity to, boolean send) throws PersistenceException {
        PersistenceDropMessage pdm = new PersistenceDropMessage(dropMessage,from, to, send, send);
        save(pdm);
    }

    @Override
    public void save(PersistenceDropMessage pdm) throws PersistenceException {
        lastMessage = pdm;

        List<PersistenceDropMessage> lst = messagesMap.get(pdm.getReceiver().getKeyIdentifier());
        if(lst == null){
            lst = new LinkedList<>();
            lst.add(pdm);
        }
        messagesMap.put(pdm.getReceiver().getKeyIdentifier(), lst);

        setChanged();
        notifyObservers(pdm);
    }

    @Override
    public List<PersistenceDropMessage> loadConversation(Contact contact, Identity identity) throws PersistenceException {
        List<PersistenceDropMessage> lst = messagesMap.get(contact.getKeyIdentifier());
        if(lst == null){
            return new LinkedList<>();
        }
        return lst;
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
