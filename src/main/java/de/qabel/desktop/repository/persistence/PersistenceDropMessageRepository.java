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
    public List<PersistenceDropMessage> loadConversation(Contact contact, Identity identity) throws PersistenceException {
        List<PersistenceDropMessage> result = new LinkedList<>();
        List<PersistenceDropMessage> messages = persistence.getEntities(PersistenceDropMessage.class);

        for (PersistenceDropMessage d : messages) {
            try {
                if (d.getSender().hashCode() == contact.hashCode() || d.getReceiver().hashCode() == contact.hashCode()) {
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


    @Override
    public List<PersistenceDropMessage> loadNewMessagesFromConversation(List<PersistenceDropMessage> dropMessages, Contact c, Identity identity) {
        List<PersistenceDropMessage> result = new LinkedList<>();

        Map<String, PersistenceDropMessage> map = new HashMap<>();

        for (PersistenceDropMessage m : dropMessages) {
            map.put(m.getPersistenceID(), m);
        }

        List<PersistenceDropMessage> messages = persistence.getEntities(PersistenceDropMessage.class);
        for (PersistenceDropMessage m : messages) {
            if (!map.containsKey(m.getPersistenceID())) {
                try {
                    if (belongsToConversation(m, c.hashCode(), identity.hashCode())) {
                        if (!isCached(m)) {
                            cache(m);
                        } else {
                            m = fromCache(m);
                        }
                        result.add(m);
                    }
                } catch (Exception e) {
                    logger.error(e.getMessage(), e);
                }
            }
        }
        return result;
    }


    private boolean belongsToConversation(PersistenceDropMessage dropMessage, int contactKeyIdentifier, int ownKeyIdentifier) {

        int senderIdentifier = dropMessage.getSender().hashCode();
        int receiverKeyIdentifier = dropMessage.getReceiver().hashCode();

        return senderIdentifier == contactKeyIdentifier && receiverKeyIdentifier == ownKeyIdentifier && !dropMessage.isSent()
                || senderIdentifier == ownKeyIdentifier && receiverKeyIdentifier == contactKeyIdentifier && dropMessage.isSent();
    }
}
