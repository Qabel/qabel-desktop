package de.qabel.desktop.config.factory;

import de.qabel.core.config.Contact;
import de.qabel.core.crypto.QblECPublicKey;
import de.qabel.core.drop.DropURL;

import java.util.Collection;

public interface ContactFactory {
    Contact createContact(QblECPublicKey publicKey, Collection<DropURL> dropUrls, String alias);
}
