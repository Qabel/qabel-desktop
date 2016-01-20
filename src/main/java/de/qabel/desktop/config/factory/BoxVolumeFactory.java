package de.qabel.desktop.config.factory;

import de.qabel.core.config.Account;
import de.qabel.core.config.Identity;
import de.qabel.desktop.config.BoxSyncConfig;
import de.qabel.desktop.storage.BoxVolume;

public interface BoxVolumeFactory {
	BoxVolume getVolume(Account account, Identity identity);
}
