package com.airhacks.afterburner.views;

import javafx.scene.Parent;

import java.util.function.Function;

public abstract class QabelFXMLView extends FXMLView {
	public QabelFXMLView() {
		super();
	}

	public QabelFXMLView(Function<String, Object> injectionContext) {
		super(injectionContext);
	}

	@Override
	void addCSSIfAvailable(Parent view) {
		addCustomCss(view);
		Parent parent = view.getParent();
		if (parent != null) {
			view.getStylesheets().addAll(parent.getStylesheets());
		}
		super.addCSSIfAvailable(view);
	}

	protected void addCustomCss(Parent view) {

	}
}
