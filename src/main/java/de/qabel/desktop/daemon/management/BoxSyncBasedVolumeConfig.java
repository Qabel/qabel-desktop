package de.qabel.desktop.daemon.management;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.DefaultAWSCredentialsProviderChain;
import de.qabel.core.crypto.QblECKeyPair;
import de.qabel.desktop.config.BoxSyncConfig;
import de.qabel.desktop.storage.BoxVolumeConfig;

import java.io.File;

public class BoxSyncBasedVolumeConfig implements BoxVolumeConfig {
	private final BoxSyncConfig boxSyncConfig;

	public BoxSyncBasedVolumeConfig(BoxSyncConfig boxSyncConfig) {
		this.boxSyncConfig = boxSyncConfig;
	}

	@Override
	public QblECKeyPair getKeyPair() {
		return boxSyncConfig.getIdentity().getPrimaryKeyPair();
	}

	@Override
	public byte[] getDeviceId() {
		return new byte[0];
	}

	@Override
	public File getTmpDir() {
		return new File(System.getProperty("java.io.tmpdir"));
	}

	@Override
	public String getPrefix() {
		return MagicEvilPrefixSource.getPrefix(boxSyncConfig.getAccount());
	}

	@Override
	public String getBucket() {
		return BoxVolumeConfig.THE_ONE_AND_ONLY_BUCKET_NAME;
	}

	@Override
	public AWSCredentials getAwsCredentials() {
		return new DefaultAWSCredentialsProviderChain().getCredentials();
	}
}
