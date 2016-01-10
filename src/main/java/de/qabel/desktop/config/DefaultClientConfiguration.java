package de.qabel.desktop.config;

import de.qabel.core.config.Account;
import de.qabel.core.config.Identity;

import java.util.Observer;

public class DefaultClientConfiguration extends ClientConfiguration {

	private ObservableObject observable;
	private Account account;
	private Identity identity;

	public DefaultClientConfiguration() {
		ensureObservable();
	}

	private void ensureObservable() {
		if (observable == null) {
			observable = new ObservableObject();
		}
	}

	@Override
	public boolean hasAccount() {
		return account != null;
	}

	@Override
	public Account getAccount() {
		return account;
	}

	@Override
	public void setAccount(Account account) throws IllegalStateException {
		if (hasAccount()) {
			throw new IllegalStateException("Account already set");
		}
		this.account = account;

		observable.setChanged();
		observable.notifyObservers(account);
	}

	@Override
	public Identity getSelectedIdentity() {
		return identity;
	}

	@Override
	public void selectIdentity(Identity identity) {
		if (!identity.equals(this.identity)) {
			observable.setChanged();
		}
		this.identity = identity;

		observable.notifyObservers(identity);
	}

	@Override
	public void addObserver(Observer o) {
		ensureObservable();
		observable.addObserver(o);
	}

	@Override
	public void deleteObserver(Observer o) {
		ensureObservable();
		observable.deleteObserver(o);
	}
}
