package de.qabel.desktop.action;

import de.qabel.core.config.Identity;
import de.qabel.core.event.identity.IdentitiesChangedEvent;
import de.qabel.core.event.identity.IdentityChangedEvent;
import org.jetbrains.annotations.NotNull;

public class IdentityAddedEvent implements IdentitiesChangedEvent, IdentityChangedEvent {
    private Identity identity;

    public IdentityAddedEvent(Identity identity) {
        this.identity = identity;
    }

    @NotNull
    @Override
    public Identity getIdentity() {
        return identity;
    }
}
