package de.qabel.desktop.storage;

import com.amazonaws.auth.DefaultAWSCredentialsProviderChain;
import de.qabel.desktop.exceptions.QblStorageException;
import org.apache.commons.io.IOUtils;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.UUID;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.fail;

public class S3BackendTest {

	@Test
	public void testSimple() throws Exception {
		String prefix = UUID.randomUUID().toString();
		StorageReadBackend readBackend = new S3ReadBackend("https://qabel.s3.amazonaws.com/" + prefix);
		StorageReadBackend bucketReadBackend = new S3ReadBackend("qabel", prefix);

		DefaultAWSCredentialsProviderChain chain = new DefaultAWSCredentialsProviderChain();
		StorageWriteBackend writeBackend = new S3WriteBackend(chain.getCredentials(), "qabel", prefix);
		byte[] bytes = new byte[]{1, 2, 3, 4};
		String name = UUID.randomUUID().toString();

		writeBackend.upload(name, new ByteArrayInputStream(bytes));
		InputStream bucketDownload = bucketReadBackend.download(name).getInputStream();
		assertArrayEquals(bytes, IOUtils.toByteArray(bucketDownload));
		InputStream download = readBackend.download(name).getInputStream();
		assertArrayEquals(bytes, IOUtils.toByteArray(download));
		writeBackend.delete(name);
		try {
			readBackend.download(name);
			fail("Download should have failed");
		} catch (QblStorageException e) {

		}
	}
}