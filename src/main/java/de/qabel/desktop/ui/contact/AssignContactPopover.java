package de.qabel.desktop.ui.contact;

import com.airhacks.afterburner.views.QabelFXMLView;
import de.qabel.core.config.Contact;
import de.qabel.desktop.ui.contact.context.AssignContactView;
import org.controlsfx.control.PopOver;

public class AssignContactPopover extends PopOver {
    public AssignContactPopover(Contact contact) {
        super(new AssignContactView(contact).getView());

        setTitle(contact.getAlias());
        setHeaderAlwaysVisible(true);
        getStyleClass().add("assignContactPopover");
        getRoot().getStyleClass().add("assignContactPopover");
        getRoot().getStylesheets().add(QabelFXMLView.getGlobalStyleSheet());
    }
}
