package de.qabel.desktop.ui.actionlog;

import de.qabel.core.config.Contact;
import de.qabel.core.config.Persistable;
import de.qabel.core.drop.DropMessage;


public class PersitsDropMessage extends Persistable {

	DropMessage dropMessage;
	Contact contact;
	Boolean send;

	public PersitsDropMessage(DropMessage dropMessage, Contact contact, Boolean send) {
		this.dropMessage = dropMessage;
		this.contact = contact;
		this.send = send;
	}

	public DropMessage getDropMessage() {
		return dropMessage;
	}

	public void setDropMessage(DropMessage dropMessage) {
		this.dropMessage = dropMessage;
	}

	public Contact getContact() {
		return contact;
	}

	public void setContact(Contact contact) {
		this.contact = contact;
	}

	public Boolean getSend() {
		return send;
	}

	public void setSend(Boolean send) {
		this.send = send;
	}
}
