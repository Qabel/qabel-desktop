package de.qabel.desktop.daemon.sync.worker;

import de.qabel.desktop.exceptions.QblStorageException;
import de.qabel.desktop.storage.cache.CachedBoxNavigation;
import de.qabel.desktop.storage.cache.CachedBoxVolume;

public class BoxVolumeStub extends CachedBoxVolume {
	public boolean indexCreated = false;
	public String rootRef = "/root/";
	public CachedBoxNavigation rootNavigation = null;

	public BoxVolumeStub() {
		super(null, null, null, null, new byte[0], null);
	}

	@Override
	public CachedBoxNavigation navigate() throws QblStorageException {
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
