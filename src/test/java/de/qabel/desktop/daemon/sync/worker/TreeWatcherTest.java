package de.qabel.desktop.daemon.sync.worker;

import de.qabel.desktop.daemon.sync.AbstractSyncTest;
import de.qabel.desktop.daemon.sync.event.ChangeEvent;
import de.qabel.desktop.daemon.sync.event.WatchEvent;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.LinkedList;
import java.util.List;

import static org.junit.Assert.assertEquals;

public class TreeWatcherTest extends AbstractSyncTest {
	private List<ChangeEvent> changes = new LinkedList<>();
	private List<WatchEvent> events = new LinkedList<>();
	private TreeWatcher watcher;

	@Before
	public void setUp() {
		super.setUp();

		watcher = new TreeWatcher(tmpDir, watchEvent -> {
			events.add(watchEvent);
			if (watchEvent instanceof ChangeEvent) {
				changes.add((ChangeEvent) watchEvent);
			}
		});
	}

	@After
	public void tearDown() throws InterruptedException {
		if (watcher != null && watcher.isAlive()) {
			watcher.interrupt();
			watcher.join();
		}
		super.tearDown();
	}

	@Test(timeout = 10000L)
	public void detectsChangedFiles() throws IOException {
		watch();

		Path file = getPath("/file");
		file.toFile().createNewFile();
		waitUntil(() -> !changes.isEmpty(), 1000L);
		assertEquals(getPath("/file"), changes.get(0).getPath());
		assertEquals(ChangeEvent.TYPE.CREATE, changes.get(0).getType());
	}

	protected Path getPath(String suffix) {
		return Paths.get(tmpDir.toAbsolutePath() + suffix);
	}

	@Test(timeout = 10000L)
	public void detectsMultipleChanges() throws IOException {
		watch();

		Path file = getPath("/file");
		file.toFile().createNewFile();
		try (BufferedWriter writer = Files.newBufferedWriter(file)) {
			writer.write("a");
		}
		waitUntil(() -> changes.size() > 1, 1000L);
		assertEquals(getPath("/file"), changes.get(0).getPath());
		assertEquals(getPath("/file"), changes.get(1).getPath());
		assertEquals(ChangeEvent.TYPE.UPDATE, changes.get(1).getType());
	}

	@Test(timeout = 10000L)
	public void detectsChangesInSubDirs() throws IOException {
		File subdir = new File(tmpDir.toFile(), "subdir");
		subdir.mkdirs();

		watch();

		File subfile = new File(subdir, "file");
		subfile.createNewFile();
		waitUntil(() -> !changes.isEmpty());
		assertEquals(getPath("/subdir/file"), changes.get(0).getPath());
	}

	@Test(timeout = 10000L)
	public void detectsChangesInNewDirs() throws Exception {
		watch();

		File subdir = new File(tmpDir.toFile(), "subdir");
		subdir.mkdirs();
		waitUntil(() -> !changes.isEmpty());

		File subfile = new File(subdir, "file");
		subfile.createNewFile();
		waitUntil(() -> changes.size() > 1);
		assertEquals(getPath("/subdir"), changes.get(0).getPath());
		assertEquals(getPath("/subdir/file"), changes.get(1).getPath());
	}

	@Test(timeout = 10000L)
	public void notifiesAboutExistingFilesAndDirs() throws Exception {
		File file = new File(tmpDir.toFile(), "existingFile");
		file.createNewFile();
		File dir = new File(tmpDir.toFile(), "existingSubdir");
		dir.mkdirs();

		watch();

		waitUntil(() -> events.size() > 2);
		assertEquals(getPath("/"), events.get(0).getPath());
		assertEquals(getPath("/existingFile"), events.get(1).getPath());
		assertEquals(getPath("/existingSubdir"), events.get(2).getPath());
	}

	@Test(timeout = 10000L)
	public void notifiesAboutFileDeletes() throws Exception {
		File file = new File(tmpDir.toFile(), "existingFile");
		file.createNewFile();
		watch();

		waitUntil(() -> events.size() == 2);
		events.clear();

		file.delete();
		waitUntil(() -> events.size() == 1);
		assertEquals(getPath("/existingFile"), events.get(0).getPath());
	}

	@Test(timeout = 10000L)
	public void notifiesAboutFileDeletesAfterTmpFileWasHandled() throws Exception {
		File file = new File(tmpDir.toFile(), "existingFile");
		file.createNewFile();
		watch();

		waitUntil(() -> events.size() == 2);
		events.clear();

		new File(tmpDir.toFile(), ".test.qpart~").createNewFile();

		Thread.sleep(1000);	// make sure both events don't occur in the same watchkey (don't know a better way...)
		file.delete();
		waitUntil(() -> events.size() >= 1);
		waitUntil(() -> events.get(0).getPath().getFileName().toString().equals("existingFile"), () -> "invalid event: " + events.get(0));
	}

	protected void watch() {
		watcher.start();
		waitUntil(watcher::isWatching);
	}
}
