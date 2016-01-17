package de.qabel.desktop.storage;

import com.amazonaws.auth.AWSCredentials;
import de.qabel.core.crypto.QblECKeyPair;

import java.io.File;

public interface BoxVolumeConfig {
	String THE_ONE_AND_ONLY_BUCKET_NAME = "qabel";

	QblECKeyPair getKeyPair();
	byte[] getDeviceId();
	File getTmpDir();
	String getPrefix();
	String getBucket();
	AWSCredentials getAwsCredentials();
}
