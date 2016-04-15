package de.qabel.desktop.config.factory;

import de.qabel.core.config.Account;

public interface AccountFactory {
    Account createAccount(String provider, String user, String auth);
}
