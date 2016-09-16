package de.qabel.desktop.storage.cache;

import de.qabel.box.storage.BoxShare;
import de.qabel.box.storage.FileMetadata;
import de.qabel.box.storage.IndexNavigation;
import de.qabel.box.storage.Share;
import de.qabel.box.storage.exceptions.QblStorageException;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.nio.file.Path;
import java.security.InvalidKeyException;
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

    @NotNull
    @Override
    public FileMetadata getMetadataFile(Share share) throws IOException, InvalidKeyException, QblStorageException {
        return nav.getMetadataFile(share);
    }
}
