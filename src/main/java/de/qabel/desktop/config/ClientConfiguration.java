package de.qabel.desktop.config;

import javafx.collections.ObservableList;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Observer;

@Deprecated
public interface ClientConfiguration extends ClientConfig{
    /**
     * @deprecated use the BoxSyncConfigRepository ()
     */
    @Deprecated
    ObservableList<BoxSyncConfig> getBoxSyncConfigs();

    /**
     * @deprecated to be replaced by more specific watchers
     *
     * @see java.util.Observable#addObserver(Observer)
     */
    @Deprecated
    void addObserver(Observer o);

    /**
     * @deprecated will be legacy when ClientConfiguration#addObserver has been replaced
     *
     * @see java.util.Observable#deleteObserver(Observer)
     */
    @Deprecated
    void deleteObserver(Observer o);

    /**
     * @deprecated query specific drops
     */
    @Deprecated
    HashMap<String, Date> getLastDropMap();

    /**
     * @deprecated set specific drop
     */
    @Deprecated
    void setLastDropMap(HashMap<String, Date> lastDropMap);

    /**
     * @deprecated set specific share
     */
    @Deprecated
    void setShareNotifications(Map<String, ShareNotifications> shareNotifications);

    /**
     * @deprecated query shares for a single identity
     */
    @Deprecated
    Map<String, ShareNotifications> getShareNotifications();
}
