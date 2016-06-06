package de.qabel.desktop.config.factory;

import de.qabel.box.storage.BoxVolume;
import de.qabel.core.config.Account;
import de.qabel.core.config.Identity;

public interface BoxVolumeFactory {
    BoxVolume getVolume(Account account, Identity identity);
}
