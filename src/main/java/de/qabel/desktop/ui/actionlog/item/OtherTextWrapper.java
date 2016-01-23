package de.qabel.desktop.ui.actionlog.item;


import de.qabel.core.config.Contact;

public class OtherTextWrapper {

	private Contact c;
	private String text;

	public Contact getContact() {
		return c;
	}

	public void setContact(Contact c) {
		this.c = c;
	}

	public String getText() {
		return text;
	}

	public void setText(String text) {
		this.text = text;
	}
}
