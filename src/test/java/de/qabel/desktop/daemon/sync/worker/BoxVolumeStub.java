package de.qabel.desktop.daemon.sync.worker;

import de.qabel.box.storage.BoxVolumeConfig;
import de.qabel.box.storage.IndexNavigation;
import de.qabel.box.storage.StorageReadBackend;
import de.qabel.box.storage.exceptions.QblStorageException;
import de.qabel.desktop.storage.cache.CachedBoxVolume;
import de.qabel.desktop.storage.cache.CachedIndexNavigation;
import org.jetbrains.annotations.NotNull;
import rx.Observable;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.stub;

public class BoxVolumeStub implements CachedBoxVolume {
    public boolean indexCreated;
    public String rootRef = "/root/";
    public CachedIndexNavigation rootNavigation;

    public BoxVolumeStub() {
        IndexNavigation mock = mock(IndexNavigation.class);
        stub(mock.getChanges()).toReturn(Observable.empty());
        rootNavigation = new BoxNavigationStub(mock, null);
    }

    @Override
    public CachedIndexNavigation navigate() throws QblStorageException {
        return rootNavigation;
    }

    @NotNull
    @Override
    public String getRootRef() {
        return rootRef;
    }


    @Override
    public StorageReadBackend getReadBackend() {
        return null;
    }

    @Override
    public void createIndex(String s, String s1) throws QblStorageException {

    }

    @Override
    public void createIndex(String s) throws QblStorageException {

    }

    @NotNull
    @Override
    public BoxVolumeConfig getConfig() {
        return null;
    }
}
