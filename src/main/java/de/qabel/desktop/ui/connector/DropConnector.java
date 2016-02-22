package de.qabel.desktop.ui.connector;

import de.qabel.core.config.Contact;
import de.qabel.core.config.Identity;
import de.qabel.core.drop.DropMessage;
import de.qabel.core.exceptions.QblNetworkInvalidResponseException;

import java.util.Date;
import java.util.List;


public interface DropConnector {
	void send(Contact c, DropMessage d) throws QblNetworkInvalidResponseException;
	List<DropMessage> receive(Identity i, Date siceDate);
}
