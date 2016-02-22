package de.qabel.desktop.storage;

import de.qabel.desktop.exceptions.QblStorageException;

import java.util.LinkedList;
import java.util.List;

public class VirtualShareNavigation implements ReadableBoxNavigation {
	private List<BoxExternal> externals = new LinkedList<>();

	public void addExternal(BoxExternal external) {
		externals.add(external);
	}

	@Override
	public BoxNavigation navigate(BoxFolder target) throws QblStorageException {
		return null;
	}

	@Override
	public BoxNavigation navigate(BoxExternal target) {
		return null;
	}

	@Override
	public List<BoxFile> listFiles() throws QblStorageException {
		return new LinkedList<>();
	}

	@Override
	public List<BoxFolder> listFolders() throws QblStorageException {
		return new LinkedList<>();
	}

	@Override
	public List<BoxExternal> listExternals() throws QblStorageException {
		return externals;
	}

	@Override
	public BoxNavigation navigate(String folderName) throws QblStorageException {
		return null;
	}

	@Override
	public BoxFolder getFolder(String name) throws QblStorageException {
		return null;
	}

	@Override
	public boolean hasFolder(String name) throws QblStorageException {
		return false;
	}

	@Override
	public BoxFile getFile(String name) throws QblStorageException {
		return null;
	}

	@Override
	public boolean hasFile(String name) throws QblStorageException {
		return false;
	}
}
