package de.qabel.desktop.repository.persistence;

import de.qabel.core.config.Contact;
import de.qabel.core.config.Entity;
import de.qabel.core.config.Identity;
import de.qabel.core.config.Persistence;
import de.qabel.core.drop.DropMessage;
import de.qabel.desktop.repository.DropMessageRepository;
import de.qabel.desktop.repository.exception.PersistenceException;
import de.qabel.desktop.ui.actionlog.PersistenceDropMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;


public class PersistenceDropMessageRepository extends AbstractCachedPersistenceRepository<PersistenceDropMessage> implements DropMessageRepository {
    private static final Logger logger = LoggerFactory.getLogger(PersistenceDropMessageRepository.class.getSimpleName());

    public PersistenceDropMessageRepository(Persistence<String> persistence) {
        super(persistence);
    }

    @Override
    public void addMessage(DropMessage dropMessage, Entity from, Entity to, boolean sent) throws PersistenceException {
        boolean seen = sent;
        PersistenceDropMessage persistenceDropMessage = new PersistenceDropMessage(dropMessage, from, to, sent, seen);
        persistence.updateOrPersistEntity(persistenceDropMessage);
        cache(persistenceDropMessage);
        setChanged();
        notifyObservers(persistenceDropMessage);
    }

    @Override
    public void save(PersistenceDropMessage message) throws PersistenceException {
        addMessage(message.getDropMessage(), message.getSender(), message.getReceiver(), message.isSent());
    }

    @Override
    public List<PersistenceDropMessage> loadConversation(Contact contact, Identity identity) throws PersistenceException {
        List<PersistenceDropMessage> result = new LinkedList<>();
        List<PersistenceDropMessage> messages = persistence.getEntities(PersistenceDropMessage.class);

        for (PersistenceDropMessage d : messages) {
            try {
                if (isSender(contact, d) && isReceiver(identity, d)
                    || isSender(identity, d) && isReceiver(contact, d)) {
                    if (!isCached(d)) {
                        cache(d);
                    } else {
                        d = fromCache(d);
                    }
                    result.add(d);
                }
            } catch (Exception e) {
                logger.error("failed to load message from conversation: " + d.getSender() + " to " + d.getReceiver());
            }
        }
        return result;
    }

    private boolean isReceiver(Entity contact, PersistenceDropMessage d) {
        return d.getReceiver().getKeyIdentifier().equals(contact.getKeyIdentifier());
    }

    private boolean isSender(Entity contact, PersistenceDropMessage d) {
        return d.getSender().getKeyIdentifier().equals(contact.getKeyIdentifier());
    }
}
