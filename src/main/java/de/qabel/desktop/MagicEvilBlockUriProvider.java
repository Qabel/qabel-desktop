package de.qabel.desktop;

import de.qabel.core.config.Account;

public class MagicEvilBlockUriProvider {
	public static String getBlockUri(Account account) {
		if (account.getProvider().contains("localhost")) {
			return "http://localhost:9697";
		} else if (account.getProvider().contains("https://test-accounting.qabel.de")) {
			return "https://test-block.qabel.de";
		} else if (account.getProvider().contains("https://qaccounting.prae.me")) {
			return "https://qblock.prae.me";
		} else {
			throw new IllegalArgumentException("don't know the block server for " + account.getProvider());
		}
	}
}
