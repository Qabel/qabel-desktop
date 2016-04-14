package de.qabel.desktop.repository;

import de.qabel.core.config.Contact;
import de.qabel.core.config.Entity;
import de.qabel.core.config.Identity;
import de.qabel.core.drop.DropMessage;
import de.qabel.desktop.repository.exception.PersistenceException;
import de.qabel.desktop.ui.actionlog.PersistenceDropMessage;

import java.util.*;

public interface DropMessageRepository {
    String PAYLOAD_TYPE_MESSAGE = "box_message";
    String PAYLOAD_TYPE_SHARE_NOTIFICATION = "box_share_notification";

    /**
     * @deprecated use PersistenceDropMessages instead (they will die too but they concat those params)
     */
    @Deprecated
    void addMessage(DropMessage dropMessage, Entity from, Entity to, boolean send) throws PersistenceException;

    void save(PersistenceDropMessage message) throws PersistenceException;

    List<PersistenceDropMessage> loadConversation(Contact contact, Identity identity) throws PersistenceException;

    /**
     * @deprecated @todo add more specific handlers
     */
    @Deprecated
    void addObserver(Observer o);

    /**
     * @deprecated @todo load by minimum timestamp or limit or paginated or both
     */
    @Deprecated
    default List<PersistenceDropMessage> loadNewMessagesFromConversation(List<PersistenceDropMessage> dropMessages, Contact c, Identity identity) throws PersistenceException {
        List<PersistenceDropMessage> result = new LinkedList<>();

        Map<String, PersistenceDropMessage> map = new HashMap<>();

        for (PersistenceDropMessage m : dropMessages) {
            map.put(m.getPersistenceID(), m);
        }

        List<PersistenceDropMessage> messages = loadConversation(c, identity);
        for (PersistenceDropMessage m : messages) {
            if (!map.containsKey(m.getPersistenceID())) {
                result.add(m);
            }
        }
        return result;
    }
}
