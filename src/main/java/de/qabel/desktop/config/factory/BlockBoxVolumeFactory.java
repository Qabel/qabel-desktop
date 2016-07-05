package de.qabel.desktop.config.factory;

import de.qabel.box.http.BlockReadBackend;
import de.qabel.box.http.BlockWriteBackend;
import de.qabel.box.storage.BoxVolume;
import de.qabel.core.accounting.AccountingHTTP;
import de.qabel.core.config.Account;
import de.qabel.core.config.Identity;
import de.qabel.core.repository.IdentityRepository;
import de.qabel.desktop.storage.cache.CachedBoxVolume;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Files;

public class BlockBoxVolumeFactory extends AbstractBoxVolumeFactory {
    private final File tmpDir;
    private final URI blockUri;
    private byte[] deviceId;

    public BlockBoxVolumeFactory(
        byte[] deviceId,
        AccountingHTTP accountingHTTP,
        IdentityRepository identityRepository,
        URI blockUri
    ) throws IOException {
        super(accountingHTTP, identityRepository);
        this.deviceId = deviceId;
        tmpDir = Files.createTempDirectory("qbl_tmp").toFile();
        this.blockUri = blockUri;
    }

    @Override
    public BoxVolume getVolume(Account account, Identity identity) {
        String prefix = super.choosePrefix(identity);

        String root = blockUri + "/api/v0/files/" + prefix + "/";

        try {
            BlockReadBackend readBackend = new BlockReadBackend(root, accountingHTTP);
            BlockWriteBackend writeBackend = new BlockWriteBackend(root, accountingHTTP);

            return new CachedBoxVolume(
                readBackend,
                writeBackend,
                identity.getPrimaryKeyPair(),
                deviceId,
                tmpDir,
                prefix
            );
        } catch (URISyntaxException e) {
            throw new IllegalStateException("couldn't create a valid block url: " + root);
        }
    }
}
