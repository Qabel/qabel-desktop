package de.qabel.desktop.storage.cache;

import de.qabel.box.storage.*;
import de.qabel.box.storage.command.*;
import de.qabel.box.storage.dto.BoxPath;
import de.qabel.box.storage.dto.DMChangeEvent;
import de.qabel.desktop.daemon.sync.event.ChangeEvent.TYPE;
import de.qabel.desktop.daemon.sync.event.RemoteChangeEvent;
import de.qabel.desktop.nio.boxfs.BoxFileSystem;
import org.jetbrains.annotations.NotNull;

import java.nio.file.Path;

import static de.qabel.desktop.daemon.sync.event.ChangeEvent.TYPE.*;

@Deprecated
public class CachedBoxNavigation {
    public static de.qabel.desktop.nio.boxfs.BoxPath getEventPath(BoxObject object, BoxNavigation navigation) {
        BoxPath.FolderLike folder = navigation.getPath();
        BoxPath.Folder eventFolder;
        if (object instanceof BoxFile) {
            eventFolder = folder.resolveFile(object.getName());
        } else {
            eventFolder = folder.resolveFolder(object.getName());
        }
        de.qabel.desktop.nio.boxfs.BoxPath newPath = BoxFileSystem.getRoot();
        for (String subpath : eventFolder.toList()) {
            newPath = newPath.resolve(subpath);
        }
        return newPath;
    }

    @NotNull
    public static RemoteChangeEvent createRemoteChangeEventFromNotification(ReadableBoxNavigation nav, DMChangeEvent it) {
        DMChange change = it.getChange();
        BoxNavigation eventNav = it.getNavigation();
        RemoteChangeEvent event = null;
        if (change instanceof UpdateFileChange) {
            UpdateFileChange update = (UpdateFileChange) change;
            BoxFile newFile = update.getNewFile();
            if (update.getExpectedFile() == null) {
                event = createRemoteChangeEvent(nav, newFile, CREATE, getEventPath(newFile, eventNav));
            } else {
                event = createRemoteChangeEvent(nav, newFile, UPDATE, getEventPath(newFile, eventNav));
            }
        } else if (change instanceof DeleteFileChange) {
            BoxFile deletedFile = ((DeleteFileChange) change).getFile();
            event = createRemoteChangeEvent(nav, deletedFile, DELETE, getEventPath(deletedFile, eventNav));
        } else if (change instanceof CreateFolderChange) {
            BoxFolder changedFolder = ((CreateFolderChange) change).getFolder();
            event = createRemoteChangeEvent(nav, changedFolder, CREATE, getEventPath(changedFolder, eventNav));
        } else if (change instanceof DeleteFolderChange) {
            BoxFolder deletedFolder = ((DeleteFolderChange) change).getFolder();
            event = createRemoteChangeEvent(nav, deletedFolder, DELETE, getEventPath(deletedFolder, eventNav));
        } else if (change instanceof ShareChange) {
            BoxFile sharedFile = ((ShareChange) change).getFile();
            event = createRemoteChangeEvent(nav, sharedFile, SHARE, getEventPath(sharedFile, eventNav));
        } else if (change instanceof UnshareChange) {
            BoxFile unsharedFile = ((UnshareChange)change).getFile();
            event = createRemoteChangeEvent(nav, unsharedFile, UNSHARE, getEventPath(unsharedFile, eventNav));
        }
        if (event == null) {
            throw new IllegalStateException("cannot create remote event from change " + change);
        }
        return event;
    }

    public static RemoteChangeEvent createRemoteChangeEvent(ReadableBoxNavigation navi, BoxObject file, TYPE type, Path targetPath) {
        Long mtime = file instanceof BoxFile ? ((BoxFile) file).getMtime() : null;
        if (type == DELETE) {
            mtime = System.currentTimeMillis();
        }
        return new RemoteChangeEvent(
            targetPath,
            file instanceof BoxFolder,
            mtime,
            type,
            file,
            navi
        );
    }
}
