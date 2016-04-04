package de.qabel.desktop.config;

import de.qabel.core.config.Account;
import de.qabel.core.config.Identity;
import javafx.collections.ObservableList;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Observer;

public interface ClientConfiguration {
    boolean hasAccount();

    Account getAccount();

    /**
     * @param account BoxAccount to use
     * @throws IllegalStateException when an account already exists
     */
    void setAccount(Account account) throws IllegalStateException;

    Identity getSelectedIdentity();

    void selectIdentity(Identity identity);

    ObservableList<BoxSyncConfig> getBoxSyncConfigs();

    /**
     * @see java.util.Observable#addObserver(Observer)
     */
    void addObserver(Observer o);

    /**
     * @see java.util.Observable#deleteObserver(Observer)
     */
    void deleteObserver(Observer o);

    boolean hasDeviceId();

    void setDeviceId(String deviceId);

    String getDeviceId();

    Date getLastDropPoll(Identity identity);

    void setLastDropPoll(Identity identity, Date lastDropPoll);

    HashMap<String, Date> getLastDropMap();

    void setLastDropMap(HashMap<String, Date> lastDropMap);

    ShareNotifications getShareNotification(Identity identity);

    void setShareNotifications(Map<String, ShareNotifications> shareNotifications);

    Map<String, ShareNotifications> getShareNotifications();
}
