package de.qabel.desktop.daemon.sync.event;

import de.qabel.desktop.daemon.sync.AbstractSyncTest;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static de.qabel.desktop.daemon.sync.event.ChangeEvent.TYPE.CREATE;
import static de.qabel.desktop.daemon.sync.event.ChangeEvent.TYPE.DELETE;
import static de.qabel.desktop.daemon.sync.event.ChangeEvent.TYPE.UPDATE;
import static org.junit.Assert.*;

public class LocalChangeEventTest extends AbstractSyncTest {
	private Path file;
	private Long mtime;

	@Before
	public void setUp() {
		super.setUp();

		file = Paths.get(tmpDir + "/testfile");
		try {
			file.toFile().createNewFile();
			mtime = Files.getLastModifiedTime(file).toMillis();
		} catch (IOException e) {
			fail("failed to create file: " + e.getMessage());
		}
	}

	@After
	public void tearDown() throws InterruptedException {
		super.tearDown();
	}

	@Test
	public void typeQueriesForCreationEvent() {
		LocalChangeEvent event = new LocalChangeEvent(file, true, mtime, CREATE);
		assertTrue(event.isCreate());
		assertFalse(event.isUpdate());
		assertFalse(event.isDelete());
	}

	@Test
	public void typeQueriesForDeletionEvent() {
		LocalChangeEvent event = new LocalChangeEvent(file, true, 0L, DELETE);
		assertTrue(event.isDelete());
		assertFalse(event.isUpdate());
		assertFalse(event.isCreate());
	}

	@Test
	public void typeQueriesForUpdateEvent() {
		LocalChangeEvent event = new LocalChangeEvent(file, true, mtime, UPDATE);
		assertTrue(event.isUpdate());
		assertFalse(event.isCreate());
		assertFalse(event.isDelete());
	}

	@Test
	public void updateTypeIsValidOnSameMtime() {
		LocalChangeEvent event = new LocalChangeEvent(file, true, mtime, UPDATE);
		assertTrue(event.isValid());
	}

	@Test
	public void updateTypeInvalidatesOnMtimeChange() {
		LocalChangeEvent event = new LocalChangeEvent(file, true, mtime, UPDATE);
		file.toFile().setLastModified(123456);
		assertFalse(event.isValid());
	}

	@Test
	public void updateTypeInvalidatesOnDelete() {
		LocalChangeEvent event = new LocalChangeEvent(file, true, mtime, UPDATE);
		file.toFile().delete();
		assertFalse(event.isValid());
	}

	@Test
	public void updateTypeDirectoryValidatesByDefault() {
		LocalChangeEvent event = new LocalChangeEvent(tmpDir, true, mtime, UPDATE);
		assertTrue(event.isValid());
	}

	@Test
	public void updateTypeDirectoryInvalidatesOnDelete() {
		Path path = Paths.get(tmpDir + "/subdir");
		path.toFile().mkdir();
		LocalChangeEvent event = new LocalChangeEvent(path, true, mtime, CREATE);
		path.toFile().delete();
		assertFalse(event.isValid());
	}

	@Test
	public void deleteTypeIsValidWhileDeleted() {
		LocalChangeEvent event = new LocalChangeEvent(Paths.get(tmpDir + "/inexistent"), true, 0L, DELETE);
		assertTrue(event.isValid());
	}

	@Test
	public void deleteTypeInvalidatesOnCreation() throws IOException {
		Path path = Paths.get(tmpDir + "/inexistent");
		LocalChangeEvent event = new LocalChangeEvent(path, true, 0L, DELETE);
		path.toFile().createNewFile();
		assertFalse(event.isValid());
	}

	@Test
	public void deleteTypeInvalidatesOnMkdir() throws IOException {
		Path path = Paths.get(tmpDir + "/subdir");
		LocalChangeEvent event = new LocalChangeEvent(path, true, 0L, DELETE);
		path.toFile().mkdir();
		assertFalse(event.isValid());
	}
}
