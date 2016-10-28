package de.qabel.desktop.ui.actionlog.item;

import com.airhacks.afterburner.views.QabelFXMLView;

import java.util.function.Function;

public class ActionlogItemView extends QabelFXMLView {
    public ActionlogItemView(Function<String, Object> injectionContext) {
        super(injectionContext);
    }
}
