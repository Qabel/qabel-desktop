package de.qabel.desktop.storage;

import com.amazonaws.auth.AWSCredentials;
import de.qabel.core.crypto.QblECKeyPair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;

public class AndroidBoxVolume extends BoxVolume {
	private static final Logger logger = LoggerFactory.getLogger(AndroidBoxVolume.class.getName());

	public AndroidBoxVolume(String bucket, String prefix, AWSCredentials credentials, QblECKeyPair keyPair, byte[] deviceId, File tempDir) {
		super(bucket, prefix, credentials, keyPair, deviceId, tempDir);
	}

	@Override
	protected void loadDriver() throws ClassNotFoundException {
		logger.info("Loading Android sqlite driver");
		Class.forName("org.sqldroid.SQLDroidDriver");
	}
}
