package de.qabel.desktop.storage.cache;

import de.qabel.desktop.exceptions.QblStorageException;
import de.qabel.desktop.storage.BoxShare;
import de.qabel.desktop.storage.IndexNavigation;

import java.nio.file.Path;
import java.util.List;

public class CachedIndexNavigation extends CachedBoxNavigation<IndexNavigation> implements IndexNavigation {
	public CachedIndexNavigation(IndexNavigation nav, Path path) {
		super(nav, path);
	}

	@Override
	public List<BoxShare> listShares() throws QblStorageException {
		return nav.listShares();
	}

	@Override
	public void insertShare(BoxShare share) throws QblStorageException {
		nav.insertShare(share);
	}

	@Override
	public void deleteShare(BoxShare share) throws QblStorageException {
		nav.deleteShare(share);
	}
}
