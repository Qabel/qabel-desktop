package de.qabel.desktop.daemon.management;

import de.qabel.box.storage.BoxVolume;
import de.qabel.box.storage.factory.BoxVolumeFactory;
import de.qabel.core.config.Account;
import de.qabel.core.config.Identity;
import de.qabel.core.config.Prefix;
import de.qabel.desktop.daemon.sync.worker.BoxVolumeStub;
import org.jetbrains.annotations.NotNull;

public class BoxVolumeFactoryStub implements BoxVolumeFactory {
    public Account lastAccount;
    public Identity lastIdentity;
    public Prefix.TYPE lastType;
    public BoxVolume boxVolume = new BoxVolumeStub();

    @Override
    public BoxVolume getVolume(Account account, Identity identity, Prefix.TYPE type) {
        lastAccount = account;
        lastIdentity = identity;
        lastType = type;
        return boxVolume;
    }

    @NotNull
    @Override
    public BoxVolume getVolume(Account account, Identity identity) {
        return getVolume(account, identity, Prefix.TYPE.USER);
    }
}
