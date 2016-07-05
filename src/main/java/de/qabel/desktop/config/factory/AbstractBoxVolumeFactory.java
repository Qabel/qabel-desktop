package de.qabel.desktop.config.factory;

import de.qabel.core.accounting.AccountingHTTP;
import de.qabel.core.config.Identity;
import de.qabel.core.exceptions.QblInvalidCredentials;
import de.qabel.core.repository.IdentityRepository;
import de.qabel.core.repository.exception.PersistenceException;

import java.io.IOException;
import java.util.ArrayList;

public abstract class AbstractBoxVolumeFactory implements BoxVolumeFactory {
    protected AccountingHTTP accountingHTTP;
    protected IdentityRepository identityRepository;

    public AbstractBoxVolumeFactory(AccountingHTTP accountingHTTP, IdentityRepository identityRepository) {
        this.accountingHTTP = accountingHTTP;
        this.identityRepository = identityRepository;
    }

    public String choosePrefix(Identity identity) {
        try {
            for (String prefix : accountingHTTP.getPrefixes()) {
                if (identity.getPrefixes().contains(prefix)) {
                    return prefix;
                }
            }

            return createNewPrefix(identity);
        } catch (Exception e) {
            throw new IllegalStateException("failed to find valid prefix: " + e.getMessage(), e);
        }
    }

    private String createNewPrefix(Identity identity) throws IOException, QblInvalidCredentials, PersistenceException {
        accountingHTTP.createPrefix();
        ArrayList<String> prefixes = accountingHTTP.getPrefixes();
        String prefix = prefixes.get(prefixes.size() - 1);
        identity.getPrefixes().add(prefix);
        identityRepository.save(identity);
        return prefix;
    }
}
