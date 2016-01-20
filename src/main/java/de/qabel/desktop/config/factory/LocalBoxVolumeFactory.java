package de.qabel.desktop.config.factory;

import de.qabel.core.config.Account;
import de.qabel.core.config.Identity;
import de.qabel.desktop.daemon.management.MagicEvilPrefixSource;
import de.qabel.desktop.storage.BoxVolume;
import de.qabel.desktop.storage.LocalReadBackend;
import de.qabel.desktop.storage.LocalWriteBackend;

import java.nio.file.Path;

public class LocalBoxVolumeFactory implements BoxVolumeFactory {
	private final Path tmpDir;
	private String prefix;

	public LocalBoxVolumeFactory(Path tmpDir) {
		this.tmpDir = tmpDir;
	}

	public LocalBoxVolumeFactory(Path tmpDir, String prefix) {
		this(tmpDir);
		this.prefix = prefix;
	}

	@Override
	public BoxVolume getVolume(Account account, Identity identity) {
		if (prefix == null) {
			prefix = MagicEvilPrefixSource.getPrefix(account);
		}
		return new BoxVolume(
				new LocalReadBackend(tmpDir),
				new LocalWriteBackend(tmpDir),
				identity.getPrimaryKeyPair(),
				new byte[0],
				tmpDir.toFile(),
				prefix
		);
	}
}
