package de.qabel.desktop.daemon.management;

import de.qabel.core.accounting.AccountingHTTP;
import de.qabel.core.accounting.AccountingProfile;
import de.qabel.core.config.Account;
import de.qabel.core.config.AccountingServer;
import de.qabel.core.exceptions.QblInvalidCredentials;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.HashMap;

/**
 * @TODO replace the accounting stuff with the new TBD version that handles all the AWS stuff itself. Then no prefix should be required anymore.
 */
@Deprecated
public class MagicEvilPrefixSource {
	private static HashMap<Account, String> prefixes = new HashMap<>();

	public synchronized static String getPrefix(Account account) {
		if (!prefixes.containsKey(account)) {
			AccountingHTTP http = null;

			try {
				http = new AccountingHTTP(new AccountingServer(new URL(account.getProvider()).toURI(), account.getUser(), account.getAuth()), new AccountingProfile());
			} catch (URISyntaxException | MalformedURLException e) {
				e.printStackTrace();
			}

			try {
				http.login();
				http.updatePrefixes();
				if (http.getProfile().getPrefixes().isEmpty()) {
					http.createPrefix();
				}
				prefixes.put(account, http.getProfile().getPrefixes().get(0));

			} catch (IOException | QblInvalidCredentials e) {
				e.printStackTrace();
			}
		}

		return prefixes.get(account);
	}

	public static void set(Account account, String prefix) {
		prefixes.put(account, prefix);
	}
}
