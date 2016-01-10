package de.qabel.desktop.ui;

import com.airhacks.afterburner.views.QabelFXMLView;
import javafx.scene.Parent;

public class LayoutView extends QabelFXMLView {
	@Override
	protected void addCustomCss(Parent view) {
		view.getStylesheets().add("/main.css");
	}
}
