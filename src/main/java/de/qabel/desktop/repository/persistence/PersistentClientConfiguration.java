package de.qabel.desktop.repository.persistence;

import de.qabel.core.config.Persistable;
import de.qabel.desktop.config.ShareNotifications;

import java.util.*;

public class PersistentClientConfiguration extends Persistable {
    private static final long serialVersionUID = 253036267706808630L;
    public String identitiyId;
    public String accountId;
    public List<PersistentBoxSyncConfig> boxSyncConfigs = new LinkedList<>();
    public HashMap<String, Date> lastDropMap;
    public String deviceId;
    public Map<String, ShareNotifications> shareNotifications = new HashMap<>();
}
