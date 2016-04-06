package de.qabel.desktop.ui.actionlog.item;

import com.airhacks.afterburner.views.QabelFXMLView;

import java.util.function.Function;

public class OtherActionlogItemView extends QabelFXMLView implements ActionlogItemView{
    public OtherActionlogItemView(Function<String, Object> injectionContext) {
        super(injectionContext);
    }
}
