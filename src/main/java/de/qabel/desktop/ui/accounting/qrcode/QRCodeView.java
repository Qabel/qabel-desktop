package de.qabel.desktop.ui.accounting.qrcode;

import com.airhacks.afterburner.views.QabelFXMLView;

import java.util.function.Function;

public class QRCodeView extends QabelFXMLView {
    public QRCodeView(Function<String, Object> injectionContext) {
        super(injectionContext);
    }
}

