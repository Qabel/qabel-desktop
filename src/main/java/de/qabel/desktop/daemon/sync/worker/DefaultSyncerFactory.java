package de.qabel.desktop.daemon.sync.worker;

import de.qabel.desktop.config.BoxSyncConfig;
import de.qabel.desktop.config.factory.BoxVolumeFactory;
import de.qabel.desktop.daemon.management.TransferManager;
import de.qabel.desktop.daemon.sync.blacklist.Blacklist;
import de.qabel.desktop.daemon.sync.blacklist.FileBasedSyncBlacklist;

import java.io.IOException;

public class DefaultSyncerFactory implements SyncerFactory {
    private TransferManager manager;

    private BoxVolumeFactory boxVolumeFactory;
    private final Blacklist blacklist;

    public DefaultSyncerFactory(BoxVolumeFactory boxVolumeFactory, TransferManager manager) throws IOException {
        this.boxVolumeFactory = boxVolumeFactory;
        this.manager = manager;
        blacklist = new FileBasedSyncBlacklist(getClass().getResourceAsStream("/ignore"));
    }

    @Override
    public Syncer factory(BoxSyncConfig config) {
        DefaultSyncer syncer = new DefaultSyncer(
                config,
                boxVolumeFactory.getVolume(config.getAccount(), config.getIdentity()),
                manager
        );
        syncer.setLocalBlacklist(blacklist);
        return syncer;
    }
}
