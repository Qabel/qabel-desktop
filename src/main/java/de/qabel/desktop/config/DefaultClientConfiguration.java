package de.qabel.desktop.config;

import de.qabel.core.config.Account;
import de.qabel.core.config.Identity;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;

import java.util.*;

public class DefaultClientConfiguration extends Observable implements ClientConfiguration {
	private Account account;
	private Identity identity;
	private HashMap<String, Date> lastDropMap = new HashMap<>();
	private Map<String, ShareNotifications> shareNotifications = new HashMap<>();

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
		if (identity == null && this.identity != null || identity != null && !identity.equals(this.identity)) {
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
	public synchronized ShareNotifications getShareNotification(Identity identity) {
		ShareNotifications shareNotifications = this.shareNotifications.get(identity.getKeyIdentifier());
		if (shareNotifications == null) {
			shareNotifications = new ShareNotifications();
			this.shareNotifications.put(identity.getKeyIdentifier(), shareNotifications);
			observeShare(shareNotifications);
		}
		return shareNotifications;
	}

	public void setShareNotifications(Map<String, ShareNotifications> shareNotifications) {
		this.shareNotifications = shareNotifications;
		shareNotifications.values().forEach(this::observeShare);
	}

	@Override
	public Map<String, ShareNotifications> getShareNotifications() {
		return shareNotifications;
	}

	private void observeShare(ShareNotifications notifications) {
		notifications.addObserver((o, arg) -> {
			setChanged();
			notifyObservers();
		});
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
