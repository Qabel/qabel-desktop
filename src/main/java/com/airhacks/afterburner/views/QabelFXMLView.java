package com.airhacks.afterburner.views;

import javafx.collections.ObservableList;
import javafx.scene.Parent;

import java.net.URL;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Function;

public abstract class QabelFXMLView extends FXMLView {
	public static List<URL> globalStyleSheets = new LinkedList<>();

	public QabelFXMLView() {
		super();
	}

	public QabelFXMLView(Function<String, Object> injectionContext) {
		super(injectionContext);
	}

	@Override
	void addCSSIfAvailable(Parent parent) {
		super.addCSSIfAvailable(parent);
		ObservableList<String> stylesheets = parent.getStylesheets();
		for (URL cssUrl : globalStyleSheets) {
			stylesheets.add(cssUrl.toExternalForm());
		}
	}

	public static void addGlobalCssFileFromResources(String path) {
		URL uri = QabelFXMLView.class.getResource(path);
		globalStyleSheets.add(uri);
	}
}
