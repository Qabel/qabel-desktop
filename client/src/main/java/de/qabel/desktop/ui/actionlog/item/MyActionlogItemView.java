package de.qabel.desktop.ui.actionlog.item;

import com.airhacks.afterburner.views.QabelFXMLView;

import java.util.function.Function;

public class MyActionlogItemView extends QabelFXMLView implements ActionlogItemView{
    public MyActionlogItemView(Function<String, Object> injectionContext) {
        super(injectionContext);
    }
}
