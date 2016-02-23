package de.qabel.desktop.daemon.sync.worker;

import de.qabel.core.crypto.QblECPublicKey;
import de.qabel.desktop.daemon.sync.event.ChangeEvent;
import de.qabel.desktop.exceptions.QblStorageException;
import de.qabel.desktop.exceptions.QblStorageNotFound;
import de.qabel.desktop.storage.*;
import de.qabel.desktop.storage.cache.CachedBoxNavigation;
import de.qabel.desktop.storage.cache.CachedIndexNavigation;

import java.nio.file.Path;
import java.util.LinkedList;
import java.util.List;

import static de.qabel.desktop.daemon.sync.event.ChangeEvent.TYPE.SHARE;
import static de.qabel.desktop.daemon.sync.event.ChangeEvent.TYPE.UNSHARE;

public class BoxNavigationStub extends CachedIndexNavigation {
	public ChangeEvent event;
	public List<BoxFolder> folders = new LinkedList<>();
	public List<BoxFile> files = new LinkedList<>();
	public List<BoxShare> shares = new LinkedList<>();

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

	@Override
	public List<BoxShare> getSharesOf(BoxObject object) throws QblStorageException {
		return shares;
	}

	@Override
	public BoxExternalReference share(QblECPublicKey owner, BoxFile file, String receiver) throws QblStorageException {
		file.setMeta(file.getBlock());
		file.setMetakey(new byte[0]);
		shares.add(new BoxShare(file.getRef(), receiver));
		notifyAsync(file, SHARE);
		return new BoxExternalReference(false, file.getRef(), file.getName(), owner, new byte[0]);
	}

	@Override
	public void unshare(BoxObject boxObject) throws QblStorageException {
		if (!(boxObject instanceof BoxFile)) {
			return;
		}
		if (!((BoxFile) boxObject).isShared()) {
			return;
		}

		BoxFile boxFile = (BoxFile)boxObject;
		shares.stream().sorted()
				.filter(boxShare -> boxFile.getRef().equals(boxShare.getRef()))
				.forEach(shares::remove);
		boxFile.setMeta(null);
		notifyAsync(boxObject, UNSHARE);
	}

	@Override
	public BoxFile getFile(String name) throws QblStorageException {
		for (BoxFile file : listFiles()) {
			if (file.getName().equals(name)) {
				return file;
			}
		}
		throw new QblStorageNotFound("no file named " + name);
	}
}
