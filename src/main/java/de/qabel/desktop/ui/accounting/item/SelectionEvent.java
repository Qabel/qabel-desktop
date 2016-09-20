package de.qabel.desktop.ui.accounting.item;


import de.qabel.core.config.Contact;
import de.qabel.desktop.ui.contact.item.ContactItemController;

public class SelectionEvent {

    private final double screenY;
    private final double screenX;
    private ContactItemController controller;
    private Contact contact;

    public SelectionEvent(double screenX, double screenY) {
        this.screenX = screenX;
        this.screenY = screenY;
    }

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

    public double getScreenY() {
        return screenY;
    }

    public double getScreenX() {
        return screenX;
    }
}
