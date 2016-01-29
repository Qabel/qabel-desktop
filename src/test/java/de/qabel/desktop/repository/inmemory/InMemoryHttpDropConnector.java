package de.qabel.desktop.repository.inmemory;

import de.qabel.core.config.Contact;
import de.qabel.core.config.Identity;
import de.qabel.core.drop.DropMessage;
import de.qabel.core.exceptions.QblNetworkInvalidResponseException;
import de.qabel.desktop.ui.connector.Connector;

import java.util.Date;
import java.util.LinkedList;
import java.util.List;


public class InMemoryHttpDropConnector implements Connector{

	List<DropMessage> lst = new LinkedList<>();

	@Override
	public void send(Contact c, DropMessage d) throws QblNetworkInvalidResponseException {
		lst.add(d);
	}

	@Override
	public List<DropMessage> receive(Identity i, Date siceDate) {
		return lst;
	}
}
