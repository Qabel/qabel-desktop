package com.airhacks.afterburner.views;

import javafx.scene.Parent;

import java.io.IOException;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.function.Function;

public class QabelFXMLView extends FXMLView {


	public QabelFXMLView() {
		super();
		this.bundle = ResourceBundle.getBundle("ui", calculateLocale(), new UTF8Converter());
	}

	public QabelFXMLView(Function<String, Object> injectionContext) {
		super(injectionContext);
		this.bundle = ResourceBundle.getBundle("ui", calculateLocale(), new UTF8Converter());
	}

	private Locale calculateLocale() {
		if(Locale.getDefault().getLanguage().equals("de")){
			return new Locale("de", "DE");
		} else {
			return new Locale("en", "EN");
		}
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
