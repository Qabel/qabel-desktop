package de.qabel.desktop.config.factory;

import de.qabel.core.config.Account;
import de.qabel.core.config.Identity;
import de.qabel.desktop.config.BoxSyncConfig;
import de.qabel.desktop.config.DefaultBoxSyncConfig;
import de.qabel.desktop.daemon.sync.worker.index.SyncIndexFactory;
import de.qabel.desktop.nio.boxfs.BoxPath;

import java.nio.file.Path;

public class DefaultBoxSyncConfigFactory implements BoxSyncConfigFactory{
    private SyncIndexFactory syncIndexFactory;

    public DefaultBoxSyncConfigFactory(SyncIndexFactory syncIndexFactory) {
        this.syncIndexFactory = syncIndexFactory;
    }

    @Override
    public BoxSyncConfig createConfig(String name, Identity identity, Account account, Path localPath, BoxPath remotePath) {
        return new DefaultBoxSyncConfig(name, localPath, remotePath, identity, account, syncIndexFactory);
    }
}
