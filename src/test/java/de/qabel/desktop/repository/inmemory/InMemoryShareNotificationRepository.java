package de.qabel.desktop.repository.inmemory;

import de.qabel.core.config.Identity;
import de.qabel.desktop.daemon.drop.ShareNotificationMessage;
import de.qabel.desktop.repository.ShareNotificationRepository;
import de.qabel.desktop.repository.exception.PersistenceException;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class InMemoryShareNotificationRepository implements ShareNotificationRepository {
    private Map<Identity, List<ShareNotificationMessage>> shares = new HashMap<>();

    @Override
    public List<ShareNotificationMessage> find(Identity identity) throws PersistenceException {
        ensureList(identity);
        return shares.get(identity);
    }

    private void ensureList(Identity identity) {
        if (!shares.containsKey(identity)) {
            shares.put(identity, new LinkedList<>());
        }
    }

    @Override
    public void save(Identity identity, ShareNotificationMessage share) throws PersistenceException {
        find(identity).add(share);
    }

    @Override
    public void delete(ShareNotificationMessage share) throws PersistenceException {
        for (List<ShareNotificationMessage> entry : shares.values()) {
            entry.remove(share);
        }
    }
}
