package de.qabel.desktop.daemon.sync.worker;

import de.qabel.box.storage.BoxVolumeConfig;
import de.qabel.box.storage.IndexNavigation;
import de.qabel.box.storage.StorageReadBackend;
import de.qabel.box.storage.exceptions.QblStorageException;
import de.qabel.desktop.storage.cache.CachedBoxVolume;
import org.jetbrains.annotations.NotNull;
import rx.Observable;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class BoxVolumeStub implements CachedBoxVolume {
    public String rootRef = "/root/";
    public IndexNavigation rootNavigation;

    public BoxVolumeStub() {
        IndexNavigation mock = mock(IndexNavigation.class);
        when(mock.getChanges()).thenReturn(Observable.empty());
        rootNavigation = mock;
    }

    @NotNull
    @Override
    public IndexNavigation navigate() throws QblStorageException {
        return rootNavigation;
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
