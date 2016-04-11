package de.qabel.desktop.repository.sqlite;

import de.qabel.core.config.Identity;
import de.qabel.desktop.daemon.drop.ShareNotificationMessage;
import de.qabel.desktop.repository.ShareNotificationRepository;
import de.qabel.desktop.repository.exception.PersistenceException;

import java.util.List;
import java.util.function.Consumer;

public class SqliteShareNotificationRepository implements ShareNotificationRepository {
    public SqliteShareNotificationRepository(ClientDatabase database) {
    }

    @Override
    public List<ShareNotificationMessage> find(Identity identity) throws PersistenceException {
        return null;
    }

    @Override
    public void save(Identity identity, ShareNotificationMessage share) throws PersistenceException {

    }

    @Override
    public void delete(ShareNotificationMessage share) throws PersistenceException {

    }

    @Override
    public void onAdd(Consumer<ShareNotificationMessage> consumer, Identity identity) {

    }

    @Override
    public void onDelete(Consumer<ShareNotificationMessage> consumer, Identity identity) {

    }
}
