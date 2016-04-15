package de.qabel.desktop.config.factory;

import de.qabel.core.config.Account;
import de.qabel.core.config.Identity;
import de.qabel.desktop.config.BoxSyncConfig;
import de.qabel.desktop.config.DefaultBoxSyncConfig;

import java.nio.file.Path;

public class DefaultBoxSyncConfigFactory implements BoxSyncConfigFactory{
    @Override
    public BoxSyncConfig createConfig(String name, Identity identity, Account account, Path localPath, Path remotePath) {
        return new DefaultBoxSyncConfig(name, localPath, remotePath, identity, account);
    }
}
