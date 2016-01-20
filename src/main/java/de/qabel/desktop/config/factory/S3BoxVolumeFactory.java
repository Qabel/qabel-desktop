package de.qabel.desktop.config.factory;

import com.amazonaws.auth.DefaultAWSCredentialsProviderChain;
import de.qabel.core.config.Account;
import de.qabel.core.config.Identity;
import de.qabel.desktop.daemon.management.MagicEvilPrefixSource;
import de.qabel.desktop.storage.BoxVolume;

import java.io.File;

public class S3BoxVolumeFactory implements BoxVolumeFactory {
	@Override
	public BoxVolume getVolume(Account account, Identity identity) {
		return new BoxVolume(
				"qabel",
				MagicEvilPrefixSource.getPrefix(account),
				new DefaultAWSCredentialsProviderChain().getCredentials(),
				identity.getPrimaryKeyPair(),
				new byte[0],
				new File(System.getProperty("java.io.tmpdir"))
		);
	}
}
