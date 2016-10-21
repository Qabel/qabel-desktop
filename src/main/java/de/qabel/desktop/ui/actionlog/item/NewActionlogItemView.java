package de.qabel.desktop.ui.actionlog.item;

import com.airhacks.afterburner.views.QabelFXMLView;

import java.util.function.Function;

public class NewActionlogItemView extends QabelFXMLView implements ActionlogItemView{
    public NewActionlogItemView(Function<String, Object> injectionContext) {
        super(injectionContext);
    }
}
