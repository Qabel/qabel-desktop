package com.airhacks.afterburner.views;

import javafx.scene.Parent;

import java.util.Locale;
import java.util.ResourceBundle;
import java.util.function.Function;

public class QabelFXMLView extends FXMLView {


	public QabelFXMLView() {
		super();
		this.bundle = ResourceBundle.getBundle("ui", Locale.getDefault(), new UTF8Converter());
	}

	public QabelFXMLView(Function<String, Object> injectionContext) {
		super(injectionContext);
		this.bundle = ResourceBundle.getBundle("ui", Locale.getDefault(), new UTF8Converter());
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
