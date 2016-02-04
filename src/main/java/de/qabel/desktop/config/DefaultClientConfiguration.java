package de.qabel.desktop.config;

import de.qabel.core.config.Account;
import de.qabel.core.config.Identity;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;

import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Observable;

public class DefaultClientConfiguration extends Observable implements ClientConfiguration {
	private Account account;
	private Identity identity;
	private HashMap<String, Date> lastDropMap = new HashMap<>();

	private ObservableList<BoxSyncConfig> boxSyncConfigs = FXCollections.synchronizedObservableList(FXCollections.observableList(new LinkedList<>()));
	private String deviceId;

	public DefaultClientConfiguration() {
		boxSyncConfigs.addListener((ListChangeListener) c -> {
			observeBoxSyncConfigs();
			boxSyncConfigWasChanged();
		});
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

	@Override
	public Date getLastDropPoll(Identity identity) {


		String key = identity.getKeyIdentifier();

		if (!lastDropMap.containsKey(key)) {
			lastDropMap.put(key, new Date(0L));
		}
		return lastDropMap.get(key);
	}

	@Override
	public void setLastDropPoll(Identity identity, Date lastDropPoll) {
		lastDropMap.put(identity.getKeyIdentifier(), lastDropPoll);
		setChanged();
		notifyObservers();
	}

	@Override
	public HashMap<String, Date> getLastDropMap() {
		return lastDropMap;
	}

	@Override
	public void setLastDropMap(HashMap<String, Date> lastDropMap) {
		if (lastDropMap == null) {
			return;
		}
		this.lastDropMap = lastDropMap;
	}


	@Override
	public boolean hasDeviceId() {
		return deviceId != null;
	}

	@Override
	public void setDeviceId(String deviceId) {
		this.deviceId = deviceId;
	}

	@Override
	public String getDeviceId() {
		return deviceId;
	}
}
