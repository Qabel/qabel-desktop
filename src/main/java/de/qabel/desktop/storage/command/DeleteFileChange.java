package de.qabel.desktop.storage.command;

import de.qabel.desktop.exceptions.QblStorageException;
import de.qabel.desktop.storage.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DeleteFileChange implements DirectoryMetadataChange {
	private static final Logger logger = LoggerFactory.getLogger(DeleteFileChange.class.getSimpleName());
	private BoxFile file;
	private IndexNavigation indexNavigation;
	private StorageWriteBackend writeBackend;

	public DeleteFileChange(BoxFile file, IndexNavigation indexNavigation, StorageWriteBackend writeBackend) {
		this.file = file;
		this.indexNavigation = indexNavigation;
		this.writeBackend = writeBackend;
	}

	@Override
	public DeleteFileChange.FileDeletionResult execute(DirectoryMetadata dm) throws QblStorageException {
		dm.deleteFile(file);

		if (file.isShared()) {
			removeSharesFromIndex();
			removeFileMetadata(dm);
		}

		return new FileDeletionResult(dm, file);
	}

	private void removeFileMetadata(DirectoryMetadata dm) throws QblStorageException {
		AbstractNavigation.removeFileMetadata(file, writeBackend, dm);
	}

	private void removeSharesFromIndex() throws QblStorageException {
		indexNavigation.getSharesOf(file).stream().forEach(share -> {
			try {
				indexNavigation.deleteShare(share);
			} catch (QblStorageException e) {
				logger.error("failed to delete share from indexNavigation: " + e.getMessage(), e);
			}
		});
	}

	public class FileDeletionResult extends ChangeResult<BoxFile> implements DeletionResult {
		public FileDeletionResult(DirectoryMetadata dm, BoxFile boxObject) {
			super(dm, boxObject);
		}

		@Override
		public String getDeletedBlockRef() {
			return "blocks/" + file.getBlock();
		}
	}
}
