package de.qabel.desktop.daemon;

import java.util.Observable;

public class NetworkStatus extends Observable {
    private boolean online = true;

    public boolean isOnline() {
        return online;
    }

    /**
     * Set status to offline
     */
    public void offline() {
        online = false;
        setChanged();
        notifyObservers();
    }

    /**
     * Set status to online
     */
    public void online() {
        online = true;
        setChanged();
        notifyObservers();
    }
}
