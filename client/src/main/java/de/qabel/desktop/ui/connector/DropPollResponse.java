package de.qabel.desktop.ui.connector;

import de.qabel.core.drop.DropMessage;

import java.util.Date;
import java.util.List;

public class DropPollResponse {
    public List<DropMessage> dropMessages;
    public Date date;

    public DropPollResponse(List<DropMessage> dropMessages, Date date) {
        this.dropMessages = dropMessages;
        this.date = date;
    }
}
