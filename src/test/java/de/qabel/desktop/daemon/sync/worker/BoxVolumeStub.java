package de.qabel.desktop.daemon.sync.worker;

import de.qabel.desktop.exceptions.QblStorageException;
import de.qabel.desktop.storage.cache.CachedBoxNavigation;
import de.qabel.desktop.storage.cache.CachedBoxVolume;
import de.qabel.desktop.storage.cache.CachedIndexNavigation;

public class BoxVolumeStub extends CachedBoxVolume {
	public boolean indexCreated = false;
	public String rootRef = "/root/";
	public CachedIndexNavigation rootNavigation = new BoxNavigationStub(null, null);

	public BoxVolumeStub() {
		super(null, null, null, null, null, null);
	}

	@Override
	public CachedIndexNavigation navigate() throws QblStorageException {
 		return rootNavigation;
	}

	@Override
	public String getRootRef() throws QblStorageException {
		return rootRef;
	}

	@Override
	public void createIndex(String bucket, String prefix) throws QblStorageException {
		indexCreated = true;
	}

	@Override
	public void createIndex(String root) throws QblStorageException {
		indexCreated = true;
	}
}
