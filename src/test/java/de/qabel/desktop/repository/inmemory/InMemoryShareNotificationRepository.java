package de.qabel.desktop.repository.inmemory;

import de.qabel.core.config.Identity;
import de.qabel.core.repository.exception.PersistenceException;
import de.qabel.desktop.daemon.drop.ShareNotificationMessage;
import de.qabel.desktop.repository.ShareNotificationRepository;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;

public class InMemoryShareNotificationRepository implements ShareNotificationRepository {
    private Map<Identity, List<Consumer<ShareNotificationMessage>>> addListeners = new HashMap<>();
    private Map<Identity, List<Consumer<ShareNotificationMessage>>> deleteListeners = new HashMap<>();

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
        List<ShareNotificationMessage> shares = find(identity);
        if (!shares.contains(share)) {
            shares.add(share);
            getAddListeners(identity).forEach(c -> c.accept(share));
        }
    }

    @Override
    public void delete(ShareNotificationMessage share) throws PersistenceException {
        for (Map.Entry<Identity, List<ShareNotificationMessage>> entry : shares.entrySet()) {
            if (entry.getValue().contains(share)) {
                entry.getValue().remove(share);
                getDeleteListeners(entry.getKey()).forEach(c -> c.accept(share));
            }
        }
    }

    @Override
    public void onAdd(Consumer<ShareNotificationMessage> consumer, Identity identity) {
        getAddListeners(identity).add(consumer);
    }

    @Override
    public void onDelete(Consumer<ShareNotificationMessage> consumer, Identity identity) {
        getDeleteListeners(identity).add(consumer);
    }

    private synchronized List<Consumer<ShareNotificationMessage>> getAddListeners(Identity identity) {
        if (!addListeners.containsKey(identity)) {
            addListeners.put(identity, new CopyOnWriteArrayList<>());
        }
        return addListeners.get(identity);
    }

    private List<Consumer<ShareNotificationMessage>> getDeleteListeners(Identity identity) {
        if (!deleteListeners.containsKey(identity)) {
            deleteListeners.put(identity, new CopyOnWriteArrayList<>());
        }
        return deleteListeners.get(identity);
    }
}
