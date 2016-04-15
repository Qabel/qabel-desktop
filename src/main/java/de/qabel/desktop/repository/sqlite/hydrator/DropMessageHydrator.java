package de.qabel.desktop.repository.sqlite.hydrator;

import de.qabel.core.config.Contact;
import de.qabel.core.config.Entity;
import de.qabel.core.config.Identity;
import de.qabel.core.drop.DropMessage;
import de.qabel.desktop.repository.EntityManager;
import de.qabel.desktop.ui.actionlog.PersistenceDropMessage;
import de.qabel.desktop.util.CheckedFunction;

import java.lang.reflect.Field;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;

public class DropMessageHydrator extends AbstractHydrator<PersistenceDropMessage> {
    private EntityManager em;
    private CheckedFunction<Integer, Identity> identityResolver;
    private CheckedFunction<Integer, Contact> contactResolver;

    public DropMessageHydrator(
        EntityManager em,
        CheckedFunction<Integer, Identity> identityResolver,
        CheckedFunction<Integer, Contact> contactResolver
    ) {
        this.em = em;
        this.identityResolver = identityResolver;
        this.contactResolver = contactResolver;
    }

    @Override
    protected String[] getFields() {
        return new String[]{"id", "receiver_id", "sender_id", "sent", "seen", "created", "payload_type", "payload"};
    }

    @Override
    public PersistenceDropMessage hydrateOne(ResultSet resultSet) throws SQLException {
        int i = 0;
        int id = resultSet.getInt(++i);
        if (em.contains(PersistenceDropMessage.class, id)) {
            return em.get(PersistenceDropMessage.class, id);
        }

        int receiverId = resultSet.getInt(++i);
        int senderId = resultSet.getInt(++i);
        boolean sent = resultSet.getBoolean(++i);
        boolean seen = resultSet.getBoolean(++i);
        Date created = new Date(resultSet.getTimestamp(++i).getTime());
        String payloadType = resultSet.getString(++i);
        String payload = resultSet.getString(++i);

        Entity sender;
        Entity receiver;
        try {
            if (sent) {
                sender = identityResolver.apply(senderId);
                receiver = contactResolver.apply(receiverId);
            } else {
                sender = contactResolver.apply(senderId);
                receiver = identityResolver.apply(receiverId);
            }
        } catch (Exception e) {
            throw new SQLException("failed to resolve relations for drop message", e);
        }

        DropMessage drop = new DropMessage(sender, payload, payloadType);
        // TODO fix this when Persistables can be modified
        try {
            Field field = DropMessage.class.getDeclaredField("created");
            field.setAccessible(true);
            field.set(drop, created);
        } catch (IllegalAccessException | NoSuchFieldException e) {
            throw new IllegalStateException("failed to set created date on DropMessage", e);
        }

        PersistenceDropMessage messsage = new PersistenceDropMessage(drop, sender, receiver, sent, seen);
        recognize(messsage);
        return messsage;
    }

    @Override
    public void recognize(PersistenceDropMessage instance) {
        em.put(PersistenceDropMessage.class, instance);
    }
}
