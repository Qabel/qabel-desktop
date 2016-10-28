package de.qabel.desktop.ui.contact.item;

import com.airhacks.afterburner.views.QabelFXMLView;
import de.qabel.core.config.Contact;

public class ContactItemView extends QabelFXMLView {
    public ContactItemView(Contact contact) {
        super(singleObjectMap("contact", contact));
    }
}
