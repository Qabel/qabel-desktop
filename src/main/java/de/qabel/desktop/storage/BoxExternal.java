package de.qabel.desktop.storage;

import de.qabel.core.crypto.QblECPublicKey;

public interface BoxExternal {
    QblECPublicKey getOwner();

    void setOwner(QblECPublicKey owner);

    boolean isAccessible();
}
