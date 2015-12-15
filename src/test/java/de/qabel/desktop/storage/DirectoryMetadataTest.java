package de.qabel.desktop.storage;

import de.qabel.core.crypto.QblECKeyPair;
import de.qabel.desktop.exceptions.QblStorageException;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.nio.ByteBuffer;
import java.sql.SQLException;
import java.util.UUID;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;

public class DirectoryMetadataTest {

	private DirectoryMetadata dm;

	@Before
	public void setUp() throws Exception {
		// device id
		UUID uuid = UUID.randomUUID();
		ByteBuffer bb = ByteBuffer.wrap(new byte[16]);
		bb.putLong(uuid.getMostSignificantBits());
		bb.putLong(uuid.getLeastSignificantBits());

		dm = DirectoryMetadata.newDatabase("https://localhost", bb.array(),
			new File(System.getProperty("java.io.tmpdir")));
	}

	@Test
	public void testInitDatabase() throws QblStorageException {
		byte[] version = dm.getVersion();
		assertThat(dm.listFiles().size(), is(0));
		assertThat(dm.listFolders().size(), is(0));
		assertThat(dm.listExternals().size(), is(0));
		dm.commit();
		assertThat(dm.getVersion(), is(not(equalTo(version))));

	}

	@Test
	public void testFileOperations() throws QblStorageException {
		BoxFile file = new BoxFile("block", "name", 0L, 0L, new byte[] {1,2,});
		dm.insertFile(file);
		assertThat(dm.listFiles().size(), is(1));
		assertThat(file, equalTo(dm.listFiles().get(0)));
		assertThat(dm.getFile("name"), is(file));
		dm.deleteFile(file);
		assertThat(dm.listFiles().size(), is(0));
		assertNull(dm.getFile("name"));
	}

	@Test
	public void testFolderOperations() throws QblStorageException {
		BoxFolder folder = new BoxFolder("block", "name", new byte[] {1,2,});
		dm.insertFolder(folder);
		assertThat(dm.listFolders().size(), is(1));
		assertThat(folder, equalTo(dm.listFolders().get(0)));
		dm.deleteFolder(folder);
		assertThat(dm.listFolders().size(), is(0));
		assertThat(dm.path.getAbsolutePath().toString(), startsWith("/tmp"));
	}

	@Test
	public void testExternalOperations() throws QblStorageException {
		BoxExternal external = new BoxExternal("https://foobar", "name",
				new QblECKeyPair().getPub(), new byte[] {1,2,});
		dm.insertExternal(external);
		assertThat(dm.listExternals().size(), is(1));
		assertThat(external, equalTo(dm.listExternals().get(0)));
		dm.deleteExternal(external);
		assertThat(dm.listExternals().size(), is(0));
	}

	@Test
	public void testLastChangedBy() throws SQLException, QblStorageException {
		assertThat(dm.deviceId, is(dm.getLastChangedBy()));
		dm.deviceId = new byte[] {1,1};
		dm.setLastChangedBy();
		assertThat(dm.deviceId, is(dm.getLastChangedBy()));
	}

	@Test
	public void testRoot() throws QblStorageException {
		assertThat(dm.getRoot(), startsWith("https://"));
	}

	@Test
	public void testSpecVersion() throws QblStorageException {
		assertThat(dm.getSpecVersion(), is(0));
	}
}