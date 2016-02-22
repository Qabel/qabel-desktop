package com.airhacks.afterburner.views;

import javafx.scene.Parent;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
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

	protected static Function<String, Object> singleObjectMap(Object... pairs) {
		Map<Object, Object> values = new HashMap<>();
		Object key = null;
		for (Object element : pairs) {
			if (key == null) {
				key = element;
				continue;
			}

			values.put(key, element);
			key = null;
		}
		return values::get;
	}

	protected static Function<String, Object> singleObjectMap(String key, Object instance) {
		return s -> {
			if (s.equals(key)) {
				return instance;
			}
			return null;
		};
	}

}
