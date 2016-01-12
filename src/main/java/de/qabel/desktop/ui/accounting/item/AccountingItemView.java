package de.qabel.desktop.ui.accounting.item;

import com.airhacks.afterburner.views.QabelFXMLView;

import java.util.function.Function;

public class AccountingItemView extends QabelFXMLView {
	public AccountingItemView(Function<String, Object> injectionContext) {
		super(injectionContext);
	}
}
