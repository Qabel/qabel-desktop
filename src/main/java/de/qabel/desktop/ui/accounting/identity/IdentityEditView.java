package de.qabel.desktop.ui.accounting.identity;

import com.airhacks.afterburner.views.QabelFXMLView;
import de.qabel.core.config.Identity;

public class IdentityEditView extends QabelFXMLView {
    public IdentityEditView(Identity identity) {
        super(singleObjectMap("identity", identity));
    }

    @Override
    public IdentityEditController getPresenter() {
        return (IdentityEditController) super.getPresenter();
    }
}
