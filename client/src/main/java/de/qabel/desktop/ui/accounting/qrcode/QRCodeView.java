package de.qabel.desktop.ui.accounting.qrcode;

import com.airhacks.afterburner.views.QabelFXMLView;
import de.qabel.core.config.Identity;

public class QRCodeView extends QabelFXMLView {
    public QRCodeView(Identity identity) {
        super(singleObjectMap("identity", identity));
    }

    @Override
    public QRCodeController getPresenter() {
        return (QRCodeController) super.getPresenter();
    }
}

