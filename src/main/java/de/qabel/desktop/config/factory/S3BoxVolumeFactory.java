package de.qabel.desktop.config.factory;

import com.amazonaws.auth.DefaultAWSCredentialsProviderChain;
import de.qabel.core.config.Account;
import de.qabel.core.config.Identity;
import de.qabel.desktop.daemon.management.MagicEvilPrefixSource;
import de.qabel.desktop.exceptions.QblStorageException;
import de.qabel.desktop.storage.BoxVolume;
import de.qabel.desktop.storage.cache.CachedBoxVolume;

import java.io.File;

public class S3BoxVolumeFactory implements BoxVolumeFactory {
	@Override
	public BoxVolume getVolume(Account account, Identity identity) {
		String prefix = MagicEvilPrefixSource.getPrefix(account);
		String bucket = "qabel";
		CachedBoxVolume volume = new CachedBoxVolume(
				bucket,
				prefix,
				new DefaultAWSCredentialsProviderChain().getCredentials(),
				identity.getPrimaryKeyPair(),
				new byte[0],
				new File(System.getProperty("java.io.tmpdir"))
		);
		return volume;
	}
}
