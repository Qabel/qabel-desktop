package de.qabel.desktop.storage.command;

import de.qabel.core.crypto.CryptoUtils;
import de.qabel.desktop.exceptions.QblStorageException;
import de.qabel.desktop.storage.BoxFolder;
import de.qabel.desktop.storage.DirectoryMetadata;
import org.spongycastle.crypto.params.KeyParameter;

public class CreateFolderChange implements DirectoryMetadataChange<ChangeResult<BoxFolder>> {
	private CryptoUtils cryptoUtils = new CryptoUtils();
	private String name;
	private byte[] deviceId;
	private final KeyParameter secretKey;

	public CreateFolderChange(String name, byte[] deviceId) {
		this.name = name;
		this.deviceId = deviceId;
		secretKey = cryptoUtils.generateSymmetricKey();
	}

	@Override
	public ChangeResult<BoxFolder> execute(DirectoryMetadata parentDM) throws QblStorageException {
		DirectoryMetadata dm = DirectoryMetadata.newDatabase(null, deviceId, parentDM.getTempDir());
		BoxFolder folder = new BoxFolder(dm.getFileName(), name, secretKey.getKey());
		parentDM.insertFolder(folder);
		dm.commit();
		return new ChangeResult<>(dm, folder);
	}
}
