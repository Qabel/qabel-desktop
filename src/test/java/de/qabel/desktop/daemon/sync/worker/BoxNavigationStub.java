package de.qabel.desktop.daemon.sync.worker;

import de.qabel.desktop.daemon.sync.event.ChangeEvent;
import de.qabel.desktop.exceptions.QblStorageException;
import de.qabel.desktop.storage.BoxNavigation;
import de.qabel.desktop.storage.cache.CachedBoxNavigation;

import java.nio.file.Path;

public class BoxNavigationStub extends CachedBoxNavigation {
	public ChangeEvent event;

	public BoxNavigationStub(BoxNavigation nav, Path path) {
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
}
