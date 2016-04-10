package de.qabel.desktop.repository;

import de.qabel.core.config.Identity;
import de.qabel.desktop.daemon.drop.ShareNotificationMessage;
import de.qabel.desktop.repository.exception.PersistenceException;

import java.util.List;

public interface ShareNotificationRepository {
    List<ShareNotificationMessage> find(Identity identity) throws PersistenceException;
    void save(Identity identity, ShareNotificationMessage share) throws PersistenceException;
    void delete(ShareNotificationMessage share) throws PersistenceException;
}
