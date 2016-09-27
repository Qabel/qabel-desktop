package de.qabel.desktop.action;

import de.qabel.core.config.Identity;
import de.qabel.core.event.identity.IdentitiesChangedEvent;
import de.qabel.core.event.identity.IdentityChangedEvent;

public class IdentityDeletedEvent implements IdentitiesChangedEvent, IdentityChangedEvent {
    private Identity identity;

    public IdentityDeletedEvent(Identity identity) {
        this.identity = identity;
    }

    @Override
    public Identity getIdentity() {
        return identity;
    }
}
