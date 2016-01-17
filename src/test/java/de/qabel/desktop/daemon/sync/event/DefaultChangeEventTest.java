package de.qabel.desktop.daemon.sync.event;

import de.qabel.desktop.daemon.sync.AbstractSyncTest;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

import static de.qabel.desktop.daemon.sync.event.ChangeEvent.TYPE.CREATE;
import static de.qabel.desktop.daemon.sync.event.ChangeEvent.TYPE.DELETE;
import static de.qabel.desktop.daemon.sync.event.ChangeEvent.TYPE.UPDATE;
import static org.junit.Assert.*;

public class DefaultChangeEventTest extends AbstractSyncTest {
	private Path file;

	@Before
	public void setUp() {
		super.setUp();

		file = Paths.get(tmpDir + "/testfile");
		try {
			file.toFile().createNewFile();
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
		DefaultChangeEvent event = new DefaultChangeEvent(file, CREATE);
		assertTrue(event.isCreate());
		assertFalse(event.isUpdate());
		assertFalse(event.isDelete());
	}

	@Test
	public void typeQueriesForDeletionEvent() {
		DefaultChangeEvent event = new DefaultChangeEvent(file, DELETE);
		assertTrue(event.isDelete());
		assertFalse(event.isUpdate());
		assertFalse(event.isCreate());
	}

	@Test
	public void typeQueriesForUpdateEvent() {
		DefaultChangeEvent event = new DefaultChangeEvent(file, UPDATE);
		assertTrue(event.isUpdate());
		assertFalse(event.isCreate());
		assertFalse(event.isDelete());
	}

	@Test
	public void updateTypeIsValidOnSameMtime() {
		DefaultChangeEvent event = new DefaultChangeEvent(file, UPDATE);
		assertTrue(event.isValid());
	}

	@Test
	public void updateTypeInvalidatesOnMtimeChange() {
		DefaultChangeEvent event = new DefaultChangeEvent(file, UPDATE);
		file.toFile().setLastModified(123456);
		assertFalse(event.isValid());
	}

	@Test
	public void updateTypeInvalidatesOnDelete() {
		DefaultChangeEvent event = new DefaultChangeEvent(file, UPDATE);
		file.toFile().delete();
		assertFalse(event.isValid());
	}

	@Test
	public void updateTypeDirectoryValidatesByDefault() {
		DefaultChangeEvent event = new DefaultChangeEvent(tmpDir, UPDATE);
		assertTrue(event.isValid());
	}

	@Test
	public void updateTypeDirectoryInvalidatesOnDelete() {
		Path path = Paths.get(tmpDir + "/subdir");
		path.toFile().mkdir();
		DefaultChangeEvent event = new DefaultChangeEvent(path, CREATE);
		path.toFile().delete();
		assertFalse(event.isValid());
	}

	@Test
	public void deleteTypeIsValidWhileDeleted() {
		DefaultChangeEvent event = new DefaultChangeEvent(Paths.get(tmpDir + "/inexistent"), DELETE);
		assertTrue(event.isValid());
	}

	@Test
	public void deleteTypeInvalidatesOnCreation() throws IOException {
		Path path = Paths.get(tmpDir + "/inexistent");
		DefaultChangeEvent event = new DefaultChangeEvent(path, DELETE);
		path.toFile().createNewFile();
		assertFalse(event.isValid());
	}

	@Test
	public void deleteTypeInvalidatesOnMkdir() throws IOException {
		Path path = Paths.get(tmpDir + "/subdir");
		DefaultChangeEvent event = new DefaultChangeEvent(path, DELETE);
		path.toFile().mkdir();
		assertFalse(event.isValid());
	}
}
