package de.qabel.desktop.repository.sqlite;

import de.qabel.core.config.Identity;
import de.qabel.desktop.daemon.drop.ShareNotificationMessage;
import de.qabel.desktop.repository.EntityManager;
import de.qabel.desktop.repository.ShareNotificationRepository;
import de.qabel.desktop.repository.exception.PersistenceException;
import de.qabel.desktop.repository.sqlite.hydrator.ShareNotificationMessageHydrator;
import org.spongycastle.util.encoders.Hex;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class SqliteShareNotificationRepository extends AbstractSqliteRepository<ShareNotificationMessage> implements ShareNotificationRepository {
    public static final String TABLE_NAME = "share_notification";
    private final Map<Identity, List<Consumer<ShareNotificationMessage>>> addListeners = new WeakHashMap<>();
    private final Map<Integer, List<Consumer<ShareNotificationMessage>>> deleteListeners = new WeakHashMap<>();

    public SqliteShareNotificationRepository(ClientDatabase database, Hydrator<ShareNotificationMessage> hydrator) {
        super(database, hydrator, TABLE_NAME);
    }

    public SqliteShareNotificationRepository(ClientDatabase database, EntityManager em) {
        this(database, new ShareNotificationMessageHydrator(em));
    }

    @Override
    public List<ShareNotificationMessage> find(Identity identity) throws PersistenceException {
        return super.findAll("identity_id = ?", identity.getId()).stream().collect(Collectors.toList());
    }

    @Override
    public void save(Identity identity, ShareNotificationMessage share) throws PersistenceException {
        if (exists(identity, share)) {
            return;
        }

        try (PreparedStatement statement = database.prepare(
            "INSERT INTO share_notification (identity_id, url, key, message) VALUES (?, ?, ?, ?)"
        )) {
            int i = 1;
            statement.setInt(i++, identity.getId());
            statement.setString(i++, share.getUrl());
            statement.setString(i++, Hex.toHexString(share.getKey().getKey()));
            statement.setString(i++, share.getMsg());
            statement.execute();
            try (ResultSet resultSet = statement.getGeneratedKeys()) {
                resultSet.next();
                share.setId(resultSet.getInt(1));
            }
            hydrator.recognize(share);
            notifyListeners(addListeners, identity, share);
        } catch (SQLException e) {
            throw new PersistenceException("failed to save ShareNotificationMessage: " + e.getMessage(), e);
        }
    }

    public boolean exists(Identity identity, ShareNotificationMessage share) {
        if (share.getId() != 0) {
            try (PreparedStatement statement = database.prepare(
                "SELECT id FROM share_notification WHERE id = ? AND identity_id = ? "
            )) {
                statement.setInt(1, share.getId());
                statement.setInt(2, identity.getId());
                try (ResultSet resultSet = statement.executeQuery()) {
                    return resultSet.next();
                }
            } catch (SQLException ignored) {}
        }
        return false;
    }

    private <T> void notifyListeners(
        Map<T, List<Consumer<ShareNotificationMessage>>> listeners,
        T identity,
        ShareNotificationMessage share
    ) {
        if (!listeners.containsKey(identity)) {
            return;
        }
        listeners.get(identity).forEach(c -> c.accept(share));
    }

    @Override
    public void delete(ShareNotificationMessage share) throws PersistenceException {
        try (PreparedStatement identitySelect = database.prepare(
            "SELECT identity_id FROM " + TABLE_NAME + " WHERE id = ?"
        )) {
            identitySelect.setInt(1, share.getId());
            try (ResultSet resultSet = identitySelect.executeQuery()) {
                int identityId = resultSet.getInt(1);

                try (PreparedStatement statement = database.prepare("DELETE FROM " + TABLE_NAME + " WHERE id = ?")) {
                    statement.setInt(1, share.getId());
                    statement.execute();
                    share.setId(0);
                    notifyListeners(deleteListeners, identityId, share);
                } catch (SQLException e) {
                    throw new PersistenceException("failed to delete share", e);
                }
            }
        } catch (SQLException e) {
            throw new PersistenceException("failed to find identity for deleted share", e);
        }
    }

    @Override
    public synchronized void onAdd(Consumer<ShareNotificationMessage> consumer, Identity identity) {
        if (!addListeners.containsKey(identity)) {
            addListeners.put(identity, new CopyOnWriteArrayList<>());
        }
        addListeners.get(identity).add(consumer);
    }

    @Override
    public synchronized void onDelete(Consumer<ShareNotificationMessage> consumer, Identity identity) {
        if (!deleteListeners.containsKey(identity.getId())) {
            deleteListeners.put(identity.getId(), new CopyOnWriteArrayList<>());
        }
        deleteListeners.get(identity.getId()).add(consumer);
    }
}
