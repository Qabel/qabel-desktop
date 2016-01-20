package de.qabel.desktop.storage;

import com.amazonaws.auth.DefaultAWSCredentialsProviderChain;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.DeleteObjectsRequest;
import com.amazonaws.services.s3.model.DeleteObjectsRequest.KeyVersion;
import com.amazonaws.services.s3.model.ObjectListing;
import com.amazonaws.services.s3.model.S3ObjectSummary;
import de.qabel.core.crypto.QblECKeyPair;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class BoxVolumeS3Test extends BoxVolumeTest {
	@Override
	protected void setUpVolume() {
		DefaultAWSCredentialsProviderChain chain = new DefaultAWSCredentialsProviderChain();

		QblECKeyPair keyPair = new QblECKeyPair();
		volume = new BoxVolume(bucket, prefix, chain.getCredentials(), keyPair, deviceID,
				new File(System.getProperty("java.io.tmpdir")));
		volume2 = new BoxVolume(bucket, prefix, chain.getCredentials(), keyPair, deviceID2,
				new File(System.getProperty("java.io.tmpdir")));

	}

	@Override
	protected void cleanVolume() {
		AmazonS3Client client = ((S3WriteBackend) volume.writeBackend).s3Client;
		ObjectListing listing = client.listObjects(bucket, prefix);
		List<KeyVersion> keys = new ArrayList<>();
		for (S3ObjectSummary summary : listing.getObjectSummaries()) {
			keys.add(new KeyVersion(summary.getKey()));
		}
		if (keys.isEmpty()) {
			return;
		}
		DeleteObjectsRequest deleteObjectsRequest = new DeleteObjectsRequest(bucket);
		deleteObjectsRequest.setKeys(keys);
		client.deleteObjects(deleteObjectsRequest);
	}
}
