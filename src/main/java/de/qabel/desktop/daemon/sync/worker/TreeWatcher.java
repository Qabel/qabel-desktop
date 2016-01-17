package de.qabel.desktop.daemon.sync.worker;

import de.qabel.desktop.daemon.sync.event.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.*;
import java.nio.file.WatchEvent;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

import static de.qabel.desktop.daemon.sync.event.ChangeEvent.TYPE.*;
import static java.nio.file.StandardWatchEventKinds.*;

/**
 * The TreeWatcher watches a fileTree for changes.
 * It will throw a ChangeEvent for each detected change, automatically watch new subdirectories for changes
 * and throw a WatchRegisteredEvent for each file or directory inside the tree, even if it already existed on start.
 */
public class TreeWatcher extends Thread {
	private Logger logger = LoggerFactory.getLogger(TreeWatcher.class);
	private Path root;
	private Consumer<de.qabel.desktop.daemon.sync.event.WatchEvent> consumer;
	private boolean watching = false;

	private WatchService watcher;
	private Map<WatchKey, Path> keys = new HashMap<>();

	public TreeWatcher(Path root, Consumer<de.qabel.desktop.daemon.sync.event.WatchEvent> consumer) {
		this.root = root;
		this.consumer = consumer;
	}

	public boolean isWatching() {
		return watching;
	}

	@Override
	@SuppressWarnings("unchecked")
	public void run() {
		try {
			watcher = FileSystems.getDefault().newWatchService();

			registerRecursive(this.root);

			watching = true;
			while (!isInterrupted()) {
				WatchKey instance = watcher.take();
				if (!instance.isValid())
					continue;

				logger.trace("fs event on " + keys.get(instance));
				processEvents(instance);
			}
		} catch (IOException e) {
			logger.error(e.getMessage(), e);
		} catch (InterruptedException e) {
			logger.warn(e.getMessage(), e);
		}
	}

	protected void processEvents(WatchKey instance) throws IOException {
		for (WatchEvent<?> event : instance.pollEvents()) {
			WatchEvent.Kind kind = event.kind();

			if (kind == OVERFLOW) {
				logger.warn("fs overflow on " + keys.get(instance));
				continue;
			}

			WatchEvent<Path> ev = (WatchEvent<Path>) event;
			Path name = ev.context();
			Path parent = keys.get(instance);
			Path child = parent.resolve(name);
			logger.trace("valid fs event on " + child + " @" + name);

			consumer.accept(new DefaultChangeEvent(child, convertType(kind)));

			if (kind == ENTRY_CREATE && Files.isDirectory(child)) {
				registerRecursive(child);
			}

			boolean valid = instance.reset();
			if (!valid) {
				keys.remove(instance);

				if (nothingToDo()) {
					break;
				}
			}
		}
	}

	protected boolean nothingToDo() {
		return keys.isEmpty();
	}

	private ChangeEvent.TYPE convertType(final WatchEvent.Kind kind) {
		if (kind == ENTRY_CREATE)
				return CREATE;
		if (kind == ENTRY_DELETE)
				return DELETE;
		if (kind == ENTRY_MODIFY)
				return UPDATE;
		throw new IllegalStateException("Unknown file modification type: " + kind);
	}

	protected void registerRecursive(Path dir) throws IOException {
		Files.walkFileTree(dir, new SimpleFileVisitor<Path>() {
			@Override
			public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
				consumer.accept(new WatchRegisteredEvent(file));
				if (watching) {
					consumer.accept(new DefaultChangeEvent(file, CREATE));
				}
				return super.visitFile(file, attrs);
			}

			@Override
			public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
				logger.trace("watching " + dir);
				WatchKey key = dir.register(watcher, ENTRY_CREATE, ENTRY_DELETE, ENTRY_MODIFY);
				keys.put(key, dir);
				consumer.accept(new WatchRegisteredEvent(dir));
				return FileVisitResult.CONTINUE;
			}
		});
	}
}
