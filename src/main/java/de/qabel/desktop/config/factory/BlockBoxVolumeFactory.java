package de.qabel.desktop.config.factory;

import de.qabel.core.accounting.AccountingHTTP;
import de.qabel.core.config.Account;
import de.qabel.core.config.Identity;
import de.qabel.desktop.daemon.management.MagicEvilPrefixSource;
import de.qabel.desktop.repository.IdentityRepository;
import de.qabel.desktop.storage.BlockReadBackend;
import de.qabel.desktop.storage.BlockWriteBackend;
import de.qabel.desktop.storage.BoxVolume;
import de.qabel.desktop.storage.cache.CachedBoxVolume;

import java.io.File;
import java.net.URISyntaxException;

public class BlockBoxVolumeFactory extends AbstractBoxVolumeFactory implements BoxVolumeFactory {
	private byte[] deviceId;

	public BlockBoxVolumeFactory(byte[] deviceId, AccountingHTTP accountingHTTP, IdentityRepository identityRepository) {
		super(accountingHTTP, identityRepository);
		this.deviceId = deviceId;
	}

	@Override
	public BoxVolume getVolume(Account account, Identity identity) {
		String prefix = super.choosePrefix(identity);

		String root;
		if (account.getProvider().contains("localhost")) {
			root = "http://localhost:9697/api/v0/files/" + prefix;
		} else if (account.getProvider().contains("https://test-accounting.qabel.de")) {
			root = "https://test-block.qabel.de/api/v0/files/" + prefix;
		} else if (account.getProvider().contains("https://qaccounting.prae.me")) {
			root = "https://qblock.prae.me/api/v0/files/" + prefix;
		} else {
			throw new IllegalArgumentException("don't know the block server for " + account.getProvider());
		}

		try {
			BlockReadBackend readBackend = new BlockReadBackend(root, accountingHTTP);
			BlockWriteBackend writeBackend = new BlockWriteBackend(root, accountingHTTP);

			return new CachedBoxVolume(
					readBackend,
					writeBackend,
					identity.getPrimaryKeyPair(),
					deviceId,
					new File(System.getProperty("java.io.tmpdir")),
					prefix
			);
		} catch (URISyntaxException e) {
			throw new IllegalStateException("couldn't create a valid block url: " + root);
		}
	}
}
