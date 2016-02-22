package de.qabel.desktop.daemon.sync.worker;

import de.qabel.desktop.daemon.sync.event.ChangeEvent;
import de.qabel.desktop.exceptions.QblStorageException;
import de.qabel.desktop.storage.BoxFile;
import de.qabel.desktop.storage.BoxFolder;
import de.qabel.desktop.storage.BoxNavigation;
import de.qabel.desktop.storage.IndexNavigation;
import de.qabel.desktop.storage.cache.CachedBoxNavigation;
import de.qabel.desktop.storage.cache.CachedIndexNavigation;

import java.nio.file.Path;
import java.util.LinkedList;
import java.util.List;

public class BoxNavigationStub extends CachedIndexNavigation {
	public ChangeEvent event;
	public List<BoxFolder> folders = new LinkedList<>();
	public List<BoxFile> files = new LinkedList<>();

	public BoxNavigationStub(IndexNavigation nav, Path path) {
		super(nav, path);
	}

	@Override
	public void refresh() throws QblStorageException {
		if (event != null) {
			setChanged();
			notifyObservers(event);
			event = null;
		}
	}

	@Override
	public void notifyAllContents() throws QblStorageException {

	}

	@Override
	public boolean hasFolder(String name) throws QblStorageException {
		return true;
	}

	@Override
	public CachedBoxNavigation navigate(String name) throws QblStorageException {
		return new BoxNavigationStub(null, getPath().resolve(name + "/"));
	}

	@Override
	public List<BoxFile> listFiles() throws QblStorageException {
		return files;
	}

	@Override
	public List<BoxFolder> listFolders() throws QblStorageException {
		return folders;
	}
}
