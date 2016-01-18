package de.qabel.desktop.ui.sync.item;

import com.airhacks.afterburner.views.QabelFXMLView;

import java.util.function.Function;

public class SyncItemView extends QabelFXMLView{
	public SyncItemView(Function<String, Object> injectionContext) {
		super(injectionContext);
	}
}
