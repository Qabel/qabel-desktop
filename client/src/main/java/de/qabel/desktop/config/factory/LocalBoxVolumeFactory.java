package de.qabel.desktop.config.factory;

import de.qabel.box.storage.BoxVolume;
import de.qabel.box.storage.BoxVolumeImpl;
import de.qabel.box.storage.LocalReadBackend;
import de.qabel.box.storage.LocalWriteBackend;
import de.qabel.core.config.Account;
import de.qabel.core.config.Identity;

import java.nio.file.Path;
import java.util.UUID;

public class LocalBoxVolumeFactory implements BoxVolumeFactory {
    private final Path tmpDir;
    private String prefix;
    private String deviceId;

    public LocalBoxVolumeFactory(Path tmpDir, String deviceId) {
        this.tmpDir = tmpDir;
        this.deviceId = deviceId;
    }

    public LocalBoxVolumeFactory(Path tmpDir, String deviceId, String prefix) {
        this(tmpDir, deviceId);
        this.prefix = prefix;
    }

    @Override
    public BoxVolume getVolume(Account account, Identity identity) {
        if (prefix == null) {
            if (identity.getPrefixes().isEmpty()) {
                identity.getPrefixes().add(UUID.randomUUID().toString());
            }
            prefix = identity.getPrefixes().get(0);
        }
        return new BoxVolumeImpl(
                new LocalReadBackend(tmpDir),
                new LocalWriteBackend(tmpDir),
                identity.getPrimaryKeyPair(),
                deviceId.getBytes(),
                tmpDir.toFile(),
                prefix
        );
    }
}
