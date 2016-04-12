package de.qabel.desktop.config.factory;

import de.qabel.core.config.Account;
import de.qabel.core.config.Identity;
import de.qabel.desktop.config.BoxSyncConfig;

import java.nio.file.Path;

public interface BoxSyncConfigFactory {
    BoxSyncConfig createConfig(String name, Identity identity, Account account, Path localPath, Path remotePath);
}
