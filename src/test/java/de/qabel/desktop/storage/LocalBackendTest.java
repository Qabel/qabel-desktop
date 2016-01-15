package de.qabel.desktop.storage;

import de.qabel.desktop.exceptions.QblStorageException;
import org.apache.commons.io.IOUtils;
import org.junit.Before;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.fail;

public class LocalBackendTest {

	private byte[] bytes;
	private String testFile;
	private StorageReadBackend readBackend;
	private StorageWriteBackend writeBackend;

	@Before
	public void setupTestBackend() throws IOException {
		Path temp = Files.createTempDirectory(null);
		Path tempFile = Files.createTempFile(temp, null, null);
		bytes = new byte[]{1, 2, 3, 4};
		Files.write(tempFile, bytes);
		readBackend = new LocalReadBackend(temp);
		testFile = tempFile.getFileName().toString();
		writeBackend = new LocalWriteBackend(temp);

	}

	@Test
	public void testReadTempFile() throws QblStorageException, IOException {
		assertArrayEquals(bytes, IOUtils.toByteArray(readBackend.download(testFile)));
	}

	@Test
	public void testWriteTempFile() throws QblStorageException, IOException {
		byte[] newBytes = bytes.clone();
		newBytes[0] = 0;
		writeBackend.upload(testFile, new ByteArrayInputStream(newBytes));
		assertArrayEquals(newBytes, IOUtils.toByteArray(readBackend.download(testFile)));
		writeBackend.delete(testFile);
		try {
			readBackend.download(testFile);
			fail("Read should have failed, file is deleted");
		} catch (QblStorageException e) {

		}

	}
}
