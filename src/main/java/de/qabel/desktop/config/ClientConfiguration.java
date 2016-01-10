package de.qabel.desktop.config;

import de.qabel.core.config.Account;
import de.qabel.core.config.Identity;
import java.util.Observer;

public abstract class ClientConfiguration {
	public abstract boolean hasAccount();
	public abstract Account getAccount();

	/**
	 * @param account BoxAccount to use
	 * @throws IllegalStateException when an account already exists
	 */
	public abstract void setAccount(Account account) throws IllegalStateException;

	public abstract Identity getSelectedIdentity();
	public abstract void selectIdentity(Identity identity);


	/**
	 * @see java.util.Observable#addObserver(Observer)
	 * @param o
	 */
	public abstract void addObserver(Observer o);

	/**
	 * @see java.util.Observable#deleteObserver(Observer)
	 * @param o
	 */
	public abstract void deleteObserver(Observer o);
}
