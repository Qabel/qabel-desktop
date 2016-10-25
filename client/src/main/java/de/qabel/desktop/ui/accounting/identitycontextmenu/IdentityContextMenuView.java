package de.qabel.desktop.ui.accounting.identitycontextmenu;

import com.airhacks.afterburner.views.QabelFXMLView;
import de.qabel.core.config.Identity;

public class IdentityContextMenuView extends QabelFXMLView {
    public IdentityContextMenuView(Identity identity) {
        super(singleObjectMap("identity", identity));
    }
}
