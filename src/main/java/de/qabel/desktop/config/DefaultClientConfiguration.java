package de.qabel.desktop.config;

import de.qabel.core.config.Account;
import de.qabel.core.config.Identity;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;

import java.util.LinkedList;
import java.util.Observable;

public class DefaultClientConfiguration extends Observable implements ClientConfiguration {
	private Account account;
	private Identity identity;
	private ObservableList<BoxSyncConfig> boxSyncConfigs = FXCollections.synchronizedObservableList(FXCollections.observableList(new LinkedList<>()));

	public DefaultClientConfiguration() {
		boxSyncConfigs.addListener((ListChangeListener) c -> {observeBoxSyncConfigs(); boxSyncConfigWasChanged(); });
		observeBoxSyncConfigs();
	}

	private void observeBoxSyncConfigs() {
		for (BoxSyncConfig conf : boxSyncConfigs) {
			conf.addObserver((o, arg) -> boxSyncConfigWasChanged());
		}
	}

	private void boxSyncConfigWasChanged() {
		setChanged();
		notifyObservers(boxSyncConfigs);
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
		this.account = account;

		setChanged();
		notifyObservers(account);
	}

	@Override
	public Identity getSelectedIdentity() {
		return identity;
	}

	@Override
	public void selectIdentity(Identity identity) {
		if (!identity.equals(this.identity)) {
			setChanged();
		}
		this.identity = identity;

		notifyObservers(identity);
	}

	@Override
	public ObservableList<BoxSyncConfig> getBoxSyncConfigs() {
		return boxSyncConfigs;
	}
}
