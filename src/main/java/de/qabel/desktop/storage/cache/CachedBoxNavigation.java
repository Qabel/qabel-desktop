package de.qabel.desktop.storage.cache;

import de.qabel.core.crypto.QblECPublicKey;
import de.qabel.desktop.daemon.sync.event.ChangeEvent.TYPE;
import de.qabel.desktop.daemon.sync.event.RemoteChangeEvent;
import de.qabel.desktop.exceptions.QblStorageException;
import de.qabel.desktop.storage.*;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.InvalidKeyException;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static de.qabel.desktop.daemon.sync.event.ChangeEvent.TYPE.CREATE;
import static de.qabel.desktop.daemon.sync.event.ChangeEvent.TYPE.DELETE;
import static de.qabel.desktop.daemon.sync.event.ChangeEvent.TYPE.UPDATE;

public class CachedBoxNavigation extends Observable implements BoxNavigation {
	private final BoxNavigation nav;
	private final BoxNavigationCache<CachedBoxNavigation> cache = new BoxNavigationCache<>();
	private final Path path;
	private static final ExecutorService executor = Executors.newCachedThreadPool();
	private BoxFolder folder;

	public CachedBoxNavigation(BoxNavigation nav, Path path) {
		this.nav = nav;
		this.path = path;
	}

	@Override
	public DirectoryMetadata reloadMetadata() throws QblStorageException {
		return nav.reloadMetadata();
	}

	@Override
	public void setMetadata(DirectoryMetadata dm) {
		nav.setMetadata(dm);
	}

	@Override
	public void commit() throws QblStorageException {
		nav.commit();
	}

	@Override
	public synchronized CachedBoxNavigation navigate(BoxFolder target) throws QblStorageException {
		if (!cache.has(target)) {
			CachedBoxNavigation subnav = new CachedBoxNavigation(
					this.nav.navigate(target),
					Paths.get(path.toString(), target.name)
			);
			cache.cache(target, subnav);
			subnav.addObserver((o, arg) -> {setChanged(); notifyObservers(arg);});
		}
		return cache.get(target);
	}

	@Override
	public BoxNavigation navigate(BoxExternal target) {
		return nav.navigate(target);
	}

	@Override
	public List<BoxFile> listFiles() throws QblStorageException {
		return nav.listFiles();
	}

	@Override
	public List<BoxFolder> listFolders() throws QblStorageException {
		return nav.listFolders();
	}

	@Override
	public List<BoxExternal> listExternals() throws QblStorageException {
		return nav.listExternals();
	}

	@Override
	public BoxFile upload(String name, File file, ProgressListener listener) throws QblStorageException {
		BoxFile upload = nav.upload(name, file, listener);
		notifyAsync(upload, CREATE);
		return upload;
	}

	@Override
	public BoxFile upload(String name, File file) throws QblStorageException {
		BoxFile upload = nav.upload(name, file);
		notifyAsync(upload, CREATE);
		return upload;
	}

	protected void notifyAsync(BoxObject file, TYPE type) {
		executor.submit(() -> notify(file, type));
	}

	@Override
	public BoxFile overwrite(String name, File file, ProgressListener listener) throws QblStorageException {
		BoxFile overwrite = nav.overwrite(name, file, listener);
		notifyAsync(overwrite, UPDATE);
		return overwrite;
	}

	@Override
	public BoxFile overwrite(String name, File file) throws QblStorageException {
		BoxFile overwrite = nav.overwrite(name, file);
		notifyAsync(overwrite, UPDATE);
		return overwrite;
	}

	@Override
	public InputStream download(BoxFile file, ProgressListener listener) throws QblStorageException {
		return nav.download(file, listener);
	}

	@Override
	public InputStream download(BoxFile file) throws QblStorageException {
		return nav.download(file);
	}

	@Override
	public BoxFolder createFolder(String name) throws QblStorageException {
		BoxFolder folder = nav.createFolder(name);
		notifyAsync(folder, CREATE);
		return folder;
	}

	@Override
	public void delete(BoxFile file) throws QblStorageException {
		nav.delete(file);
		notifyAsync(file, DELETE);
	}

	@Override
	public void delete(BoxFolder folder) throws QblStorageException {
		nav.delete(folder);
		cache.remove(folder);
		notifyAsync(folder, DELETE);
	}

	@Override
	public void delete(BoxExternal external) throws QblStorageException {
		nav.delete(external);
	}

	@Override
	public void setAutocommit(boolean autocommit) {
		nav.setAutocommit(autocommit);
	}

	@Override
	public CachedBoxNavigation navigate(String folderName) throws QblStorageException {
		return navigate(getFolder(folderName));
	}

	@Override
	public BoxFolder getFolder(String name) throws QblStorageException {
		return nav.getFolder(name);
	}

	@Override
	public boolean hasFolder(String name) throws QblStorageException {
		return nav.hasFolder(name);
	}

	@Override
	public BoxFile getFile(String name) throws QblStorageException {
		return nav.getFile(name);
	}

	@Override
	public DirectoryMetadata getMetadata() {
		return nav.getMetadata();
	}

	@Override
	public BoxExternalReference createFileMetadata(QblECPublicKey owner, BoxFile boxFile) throws QblStorageException {
		return nav.createFileMetadata(owner, boxFile);
	}

	@Override
	public void updateFileMetadata(BoxFile boxFile) throws QblStorageException, IOException, InvalidKeyException {
		nav.updateFileMetadata(boxFile);
	}

	public void refresh() throws QblStorageException {
		synchronized (this) {
			synchronized (nav) {
				DirectoryMetadata dm = nav.reloadMetadata();
				if (!Arrays.equals(nav.getMetadata().getVersion(), dm.getVersion())) {
					Set<BoxFolder> oldFolders = new HashSet<>(nav.listFolders());
					Set<BoxFile> oldFiles = new HashSet<>(nav.listFiles());

					nav.setMetadata(dm);

					Set<BoxFolder> newFolders = new HashSet<>(nav.listFolders());
					Set<BoxFile> newFiles = new HashSet<>(nav.listFiles());
					Set<BoxFile> changedFiles = new HashSet<>();

					findNewFolders(oldFolders, newFolders);
					findNewFiles(oldFiles, newFiles, changedFiles);
					findDeletedFolders(oldFolders, newFolders);
					findDeletedFiles(oldFiles, newFiles, changedFiles);
				}
			}
		}

		for (BoxFolder folder : listFolders()) {
			try {
				navigate(folder).refresh();
			} catch (QblStorageException e) {
				System.err.println(path.toString() + "/" + folder.name + ": " + e.getMessage());
			}
		}
	}

	@Override
	public boolean hasFile(String name) throws QblStorageException {
		return nav.hasFile(name);
	}

	protected void findDeletedFiles(Set<BoxFile> oldFiles, Set<BoxFile> newFiles, Set<BoxFile> changedFiles) {
		for (BoxFile file : oldFiles) {
			if (changedFiles.contains(file)) {
				continue;
			}
			if (!newFiles.contains(file)) {
				TYPE type = TYPE.DELETE;
				notify(file, type);
			}
		}
	}

	private void notify(BoxObject file, TYPE type) {
		setChanged();
		Long mtime = file instanceof BoxFile ? ((BoxFile) file).mtime : null;
		if (type == DELETE) {
			mtime = System.currentTimeMillis();
		}
		notifyObservers(
				new RemoteChangeEvent(
						getPath(file),
						file instanceof BoxFolder,
						mtime,
						type,
						file,
						this
				)
		);
	}

	protected void findDeletedFolders(Set<BoxFolder> oldFolders, Set<BoxFolder> newFolders) {
		for (BoxFolder folder : oldFolders) {
			if (!newFolders.contains(folder)) {
				notify(folder, TYPE.DELETE);
			}
		}
	}

	protected void findNewFiles(Set<BoxFile> oldFiles, Set<BoxFile> newFiles, Set<BoxFile> changedFiles) {
		for (BoxFile file : newFiles) {
			if (!oldFiles.contains(file)) {
				TYPE type = CREATE;
				for (BoxFile oldFile : oldFiles) {
					if (oldFile.name.equals(file.name)) {
						type = UPDATE;
						changedFiles.add(oldFile);
						break;
					}
				}
				notify(file, type);
			}
		}
	}

	protected void findNewFolders(Set<BoxFolder> oldFolders, Set<BoxFolder> newFolders) throws QblStorageException {
		for (BoxFolder folder : newFolders) {
			if (!oldFolders.contains(folder)) {
				notify(folder, CREATE);
				navigate(folder).notifyAllContents();
			}
		}
	}

	public void notifyAllContents() throws QblStorageException {
		for (BoxFolder folder : nav.listFolders()) {
			notify(folder, CREATE);
			navigate(folder).notifyAllContents();
		}
		for (BoxFile file : listFiles()) {
			notify(file, CREATE);
		}
	}

	public Path getPath() {
		return path;
	}

	public Path getPath(BoxObject folder) {
		return Paths.get(path.toString(), folder.name);
	}
}
