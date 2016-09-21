package de.qabel.desktop.daemon.sync.worker;

import de.qabel.box.storage.*;
import de.qabel.box.storage.exceptions.QblStorageException;
import de.qabel.box.storage.exceptions.QblStorageNotFound;
import de.qabel.core.crypto.QblECPublicKey;
import de.qabel.desktop.daemon.sync.event.ChangeEvent;
import de.qabel.desktop.daemon.sync.event.ChangeEvent.TYPE;
import de.qabel.desktop.storage.cache.CachedBoxNavigation;
import de.qabel.desktop.storage.cache.CachedIndexNavigation;

import java.nio.file.Path;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import static de.qabel.desktop.daemon.sync.event.ChangeEvent.TYPE.SHARE;
import static de.qabel.desktop.daemon.sync.event.ChangeEvent.TYPE.UNSHARE;

public class BoxNavigationStub extends CachedIndexNavigation {
    public ChangeEvent event;
    public List<BoxFolder> folders = new LinkedList<>();
    public List<BoxFile> files = new LinkedList<>();
    public List<BoxShare> shares = new LinkedList<>();
    public Map<String, BoxNavigationStub> subnavs = new HashMap<>();

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

    public void pushNotification(BoxObject object, TYPE type) {
        notifyAsync(object, type);
    }

    @Override
    public boolean hasFolder(String name) throws QblStorageException {
        return true;
    }

    @Override
    public BoxNavigationStub navigate(String name) throws QblStorageException {
        if (!subnavs.containsKey(name)) {
            subnavs.put(name, new BoxNavigationStub(null, getDesktopPath().resolve(name + "/")));
        }
        return subnavs.get(name);
    }

    @Override
    public synchronized CachedBoxNavigation navigate(BoxFolder target) throws QblStorageException {
        return navigate(target.getName());
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
        file.setShared(new Share(file.getBlock(), new byte[0]));
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

        BoxFile boxFile = (BoxFile) boxObject;
        shares.stream().sorted()
            .filter(boxShare -> boxFile.getRef().equals(boxShare.getRef()))
            .forEach(shares::remove);
        boxFile.setShared(null);
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
