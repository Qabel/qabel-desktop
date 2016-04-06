package de.qabel.desktop.storage.command;

import de.qabel.desktop.exceptions.QblStorageException;
import de.qabel.desktop.storage.BoxFolder;
import de.qabel.desktop.storage.DirectoryMetadata;

public class DeleteFolderChange implements DirectoryMetadataChange<DeleteFolderChange.FolderDeletionResult> {
    private BoxFolder folder;

    public DeleteFolderChange(BoxFolder folder) {
        this.folder = folder;
    }

    @Override
    public FolderDeletionResult execute(DirectoryMetadata dm) throws QblStorageException {
        dm.deleteFolder(folder);
        return new FolderDeletionResult(dm, folder);
    }

    public class FolderDeletionResult extends ChangeResult<BoxFolder> implements DeletionResult {
        public FolderDeletionResult(DirectoryMetadata dm, BoxFolder folder) {
            super(dm, folder);
        }

        @Override
        public String getDeletedBlockRef() {
            return folder.getRef();
        }
    }
}
