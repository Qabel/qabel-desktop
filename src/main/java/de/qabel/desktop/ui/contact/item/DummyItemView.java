package de.qabel.desktop.ui.contact.item;

import com.airhacks.afterburner.views.QabelFXMLView;

import java.util.function.Function;

public class DummyItemView extends QabelFXMLView {
	public DummyItemView(Function<String, Object> injectionContext) {
		super(injectionContext);
	}
}
