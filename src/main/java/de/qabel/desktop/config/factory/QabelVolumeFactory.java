package de.qabel.desktop.config.factory;

import de.qabel.core.accounting.AccountingHTTP;
import de.qabel.core.config.Account;
import de.qabel.core.config.Identity;
import de.qabel.desktop.daemon.management.MagicEvilPrefixSource;
import de.qabel.desktop.storage.*;
import de.qabel.desktop.storage.cache.CachedBoxVolume;

import java.io.File;

public class QabelVolumeFactory implements BoxVolumeFactory {
	private final AccountingHTTP accountingHTTP;
	private String prefix;

	public QabelVolumeFactory(AccountingHTTP accountingHTTP) {
		this.accountingHTTP = accountingHTTP;
	}

	public QabelVolumeFactory(AccountingHTTP accountingHTTP, String prefix) {
		this(accountingHTTP);
		this.prefix = prefix;
	}

	@Override
	public BoxVolume getVolume(Account account, Identity identity) {
		if (prefix == null) {
			prefix = MagicEvilPrefixSource.getPrefix(account);
		}
		return new CachedBoxVolume(
				new QabelReadBackend(accountingHTTP, prefix),
				new QabelWriteBackend(accountingHTTP, prefix),
				identity.getPrimaryKeyPair(),
				new byte[0],
				new File(System.getProperty("java.io.tmpdir")),
				prefix
		);
	}
}
