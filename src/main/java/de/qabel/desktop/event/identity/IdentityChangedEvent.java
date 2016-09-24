package de.qabel.desktop.event.identity;

import de.qabel.core.config.Identity;
import de.qabel.desktop.event.Event;

public interface IdentityChangedEvent extends Event {
    Identity getIdentity();
}
