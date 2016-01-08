package de.qabel.desktop.ui;

import javafx.geometry.Insets;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import org.apache.log4j.Logger;

import java.io.PrintWriter;
import java.io.StringWriter;

public class AbstractController {
	protected void alert(String message, Exception e) {
		Logger.getLogger(getClass().getSimpleName()).error(message, e);

		Alert alert = new Alert(Alert.AlertType.ERROR);
		alert.setTitle("Error");
		alert.setHeaderText(message);

		TextArea textArea = new TextArea(getTraceAsString(e));
		VBox.setMargin(textArea, new Insets(10, 0, 5, 0));
		textArea.setEditable(false);
		textArea.setWrapText(false);

		VBox.setVgrow(textArea, Priority.ALWAYS);

		Label label = new Label(e.getMessage());
		VBox expansion = new VBox();
		expansion.getChildren().add(label);
		expansion.getChildren().add(textArea);

		alert.getDialogPane().setContent(expansion);
		alert.setResizable(true);
		alert.showAndWait();
	}

	private String getTraceAsString(Exception e) {
		StringWriter sw = new StringWriter();
		PrintWriter pw = new PrintWriter(sw);
		e.printStackTrace(pw);
		return sw.toString();
	}
}
