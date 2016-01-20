package de.qabel.desktop.daemon.sync.worker;

import de.qabel.desktop.exceptions.QblStorageException;
import de.qabel.desktop.storage.BoxNavigation;
import de.qabel.desktop.storage.BoxVolume;

public class BoxVolumeStub extends BoxVolume {
	public boolean indexCreated = false;
	public String rootRef = "/root/";
	public BoxNavigation rootNavigation = null;

	public BoxVolumeStub() {
		super(null, null, null, null, new byte[0], null);
	}

	@Override
	public BoxNavigation navigate() throws QblStorageException {
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
