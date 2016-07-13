package de.qabel.desktop.ui.connector;

import de.qabel.core.config.Contact;
import de.qabel.core.config.Identity;
import de.qabel.core.drop.DropMessage;
import de.qabel.core.exceptions.QblNetworkInvalidResponseException;

import java.util.Date;

public interface DropConnector {
    void send(Contact c, DropMessage d) throws QblNetworkInvalidResponseException;
    DropPollResponse receive(Identity i, Date siceDate);
}
