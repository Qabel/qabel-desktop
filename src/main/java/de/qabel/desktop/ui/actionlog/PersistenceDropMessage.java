package de.qabel.desktop.ui.actionlog;

import de.qabel.core.config.Entity;
import de.qabel.core.config.Persistable;
import de.qabel.core.drop.DropMessage;


public class PersistenceDropMessage extends Persistable {

	DropMessage dropMessage;
	Entity receiver;
	Entity sender;
	Boolean send;

	public PersistenceDropMessage(DropMessage dropMessage, Entity from,  Entity to, Boolean send) {
		this.dropMessage = dropMessage;
		this.receiver = to;
		this.send = send;
		this.sender = from;
	}

	public Entity getSender() {
		return sender;
	}

	public void setSender(Entity sender) {
		this.sender = sender;
	}

	public DropMessage getDropMessage() {
		return dropMessage;
	}

	public void setDropMessage(DropMessage dropMessage) {
		this.dropMessage = dropMessage;
	}

	public Entity getReceiver() {
		return receiver;
	}

	public void setReceiver(Entity receiver) {
		this.receiver = receiver;
	}

	public Boolean getSend() {
		return send;
	}

	public void setSend(Boolean send) {
		this.send = send;
	}
}
