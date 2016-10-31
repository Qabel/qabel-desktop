package de.qabel.desktop.daemon.drop;

import de.qabel.core.event.Event;
import de.qabel.desktop.ui.actionlog.PersistenceDropMessage;

public class MessageReceivedEvent implements Event {
    private PersistenceDropMessage message;

    public MessageReceivedEvent(PersistenceDropMessage message) {
        this.message = message;
    }

    public PersistenceDropMessage getMessage() {
        return message;
    }
}
