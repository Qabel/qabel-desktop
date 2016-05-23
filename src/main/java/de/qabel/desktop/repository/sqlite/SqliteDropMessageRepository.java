package de.qabel.desktop.repository.sqlite;

import de.qabel.core.config.Contact;
import de.qabel.core.config.Entity;
import de.qabel.core.config.Identity;
import de.qabel.core.drop.DropMessage;
import de.qabel.desktop.repository.DropMessageRepository;
import de.qabel.desktop.repository.EntityManager;
import de.qabel.desktop.repository.exception.PersistenceException;
import de.qabel.desktop.repository.sqlite.hydrator.DropMessageHydrator;
import de.qabel.desktop.ui.actionlog.PersistenceDropMessage;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.List;
import java.util.Observer;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;

public class SqliteDropMessageRepository extends AbstractSqliteRepository<PersistenceDropMessage> implements DropMessageRepository {
    public static final String TABLE_NAME = "drop_message";
    private List<Observer> observers = new CopyOnWriteArrayList<>();

    public SqliteDropMessageRepository(ClientDatabase database, Hydrator<PersistenceDropMessage> hydrator) {
        super(database, hydrator, TABLE_NAME);
    }

    public SqliteDropMessageRepository(
        ClientDatabase database,
        EntityManager em,
        SqliteIdentityRepository identityRepo,
        SqliteContactRepository contactRepo
    ) {
        this(
            database,
            new DropMessageHydrator(
                em,
                id -> em.contains(Identity.class, id) ? em.get(Identity.class, id) : identityRepo.find(id),
                id -> em.contains(Contact.class, id) ? em.get(Contact.class, id) : contactRepo.find(id)
            )
        );
    }

    public SqliteDropMessageRepository(ClientDatabase database, EntityManager em) {
        this(
            database,
            em,
            new SqliteIdentityRepository(database, em),
            new SqliteContactRepository(database, em)
        );
    }

    @Override
    public void addMessage(DropMessage dropMessage, Entity from, Entity to, boolean send) throws PersistenceException {
        PersistenceDropMessage message = new PersistenceDropMessage(dropMessage, from, to, send, send);
        save(message);
    }

    @Override
    public void save(PersistenceDropMessage message) throws PersistenceException {
        if (message.getId() == 0) {
            insert(message);
        } else {
            update(message);
        }
    }

    private void update(PersistenceDropMessage message) throws PersistenceException {
        try (PreparedStatement statement = database.update("drop_message")
             .set("receiver_id")
             .set("sender_id")
             .set("sent")
             .set("seen")
             .set("created")
             .set("payload_type")
             .set("payload")
             .where("id = ?")
             .build()
        ) {
            int i = 0;
            statement.setInt(++i, message.getReceiver().getId());
            statement.setInt(++i, message.getSender().getId());
            statement.setBoolean(++i, message.isSent());
            statement.setBoolean(++i, message.isSeen());
            statement.setTimestamp(++i, Timestamp.from(message.getDropMessage().getCreationDate().toInstant()));
            statement.setString(++i, message.getDropMessage().getDropPayloadType());
            statement.setString(++i, message.getDropMessage().getDropPayload());
            statement.setInt(++i, message.getId());
            statement.execute();
        } catch (SQLException e) {
            throw new PersistenceException("failed to update message " + message, e);
        }
    }

    private void insert(PersistenceDropMessage message) throws PersistenceException {
        try (PreparedStatement statement = database.prepare(
            "INSERT INTO drop_message (receiver_id, sender_id, sent, seen, created, payload_type, payload) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?)"
        )) {
            int i = 0;
            DropMessage drop = message.getDropMessage();
            statement.setInt(++i, message.getReceiver().getId());
            statement.setInt(++i, message.getSender().getId());
            statement.setBoolean(++i, message.isSent());
            statement.setBoolean(++i, message.isSeen());
            statement.setTimestamp(++i, Timestamp.from(drop.getCreationDate().toInstant()));
            statement.setString(++i, drop.getDropPayloadType());
            statement.setString(++i, drop.getDropPayload());
            statement.execute();
            try (ResultSet resultSet = statement.getGeneratedKeys()) {
                resultSet.next();
                message.setId(resultSet.getInt(1));
                hydrator.recognize(message);
                observers.forEach(o -> o.update(null, message));
            }
        } catch (SQLException e) {
            throw new PersistenceException("failed to save drop message", e);
        }
    }

    @Override
    public List<PersistenceDropMessage> loadConversation(Contact contact, Identity identity) throws PersistenceException {
        try (PreparedStatement statement = database.selectFrom("drop_message", "d")
             .select(hydrator.getFields("d"))
             .join("identity i ON (i.id = ? AND (i.id = d.receiver_id OR i.id = d.sender_id))")
             .join("contact c ON (c.id = ? AND (c.id = d.receiver_id OR c.id = d.sender_id))")
             .orderBy("d.created ASC")
             .build()
        ) {
            statement.setMaxRows(100);
            statement.setInt(1, identity.getId());
            statement.setInt(2, contact.getId());
            try (ResultSet resultSet = statement.executeQuery()) {
                return hydrator.hydrateAll(resultSet).stream().collect(Collectors.toList());
            }
        } catch (SQLException e) {
            throw new PersistenceException("failed to load conversation", e);
        }
    }

    @Override
    public void addObserver(Observer o) {
        observers.add(o);
    }
}
