package de.qabel.desktop.ui.contact.context;

import com.airhacks.afterburner.views.QabelFXMLView;
import de.qabel.core.config.Contact;

public class AssignContactView extends QabelFXMLView {
    public AssignContactView(Contact contact) {
        super(singleObjectMap("contact", contact));
    }

    @Override
    public AssignContactController getPresenter() {
        return (AssignContactController) super.getPresenter();
    }
}
