package de.qabel.desktop.config.factory;

import de.qabel.core.config.Account;
import de.qabel.core.config.Identity;
import de.qabel.desktop.storage.BoxVolume;

import java.util.HashMap;
import java.util.Map;

public class CachedBoxVolumeFactory implements BoxVolumeFactory {
	private BoxVolumeFactory factory;
	private Map<Account, Map<Identity, BoxVolume>> volumes = new HashMap<>();

	public CachedBoxVolumeFactory(BoxVolumeFactory factory) {
		this.factory = factory;
	}

	@Override
	public synchronized BoxVolume getVolume(Account account, Identity identity) {
		if (!volumes.containsKey(account) || !volumes.get(account).containsKey(identity)) {
			if (!volumes.containsKey(account)) {
				volumes.put(account, new HashMap<>());
			}

			volumes.get(account).put(identity, factory.getVolume(account, identity));
		}

		return volumes.get(account).get(identity);
	}
}
