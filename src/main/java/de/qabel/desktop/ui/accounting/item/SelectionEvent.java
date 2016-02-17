package de.qabel.desktop.ui.accounting.item;


import de.qabel.core.config.Contact;
import de.qabel.desktop.ui.contact.item.ContactItemController;

public class SelectionEvent {

	private ContactItemController controller;
	private Contact contact;

	public Contact getContact() {
		return contact;
	}

	public void setContact(Contact contact) {
		this.contact = contact;
	}

	public ContactItemController getController() {
		return controller;
	}

	public void setController(ContactItemController controller) {
		this.controller = controller;
	}
}
