package de.qabel.desktop.ui.contact.index.result;

import com.airhacks.afterburner.views.QabelFXMLView;
import de.qabel.core.config.Contact;
import javafx.scene.layout.Pane;

public class IndexSearchResultItemView extends QabelFXMLView {
    public IndexSearchResultItemView(Contact contact) {
        super(singleObjectMap("contact", contact));
    }

    /**
     * Get the view async and put it into the parent
     */
    public IndexSearchResultItemView place(Pane parent) {
        getViewAsync(parent.getChildren()::add);
        return this;
    }
}
