package de.qabel.desktop.config;

import de.qabel.core.config.Account;
import de.qabel.core.config.Identity;
import javafx.collections.ObservableList;

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
	 * @param o
	 * @see java.util.Observable#addObserver(Observer)
	 */
	void addObserver(Observer o);

	/**
	 * @param o
	 * @see java.util.Observable#deleteObserver(Observer)
	 */
	void deleteObserver(Observer o);

	boolean hasDeviceId();

	void setDeviceId(String deviceId);

	String getDeviceId();
}
