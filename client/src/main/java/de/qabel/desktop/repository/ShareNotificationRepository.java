package de.qabel.desktop.repository;

import de.qabel.core.config.Identity;
import de.qabel.core.repository.exception.PersistenceException;
import de.qabel.desktop.daemon.drop.ShareNotificationMessage;

import java.util.List;
import java.util.function.Consumer;

public interface ShareNotificationRepository {
    List<ShareNotificationMessage> find(Identity identity) throws PersistenceException;
    void save(Identity identity, ShareNotificationMessage share) throws PersistenceException;
    void delete(ShareNotificationMessage share) throws PersistenceException;

    void onAdd(Consumer<ShareNotificationMessage> consumer, Identity identity);
    void onDelete(Consumer<ShareNotificationMessage> consumer, Identity identity);
}
