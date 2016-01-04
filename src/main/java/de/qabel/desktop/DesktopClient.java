package de.qabel.desktop;

import javafx.application.Application;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;

public class DesktopClient extends Application {
	private static final String TITLE = "Qabel Desktop Client";

	public static void main(String[] args) throws Exception {
		launch(args);
	}

	@Override
	public void start(Stage primaryStage) throws Exception {
		setUserAgentStylesheet(STYLESHEET_MODENA);

		Rectangle rect = new Rectangle(40, 40);
		Group root = new Group(rect);
		Scene scene = new Scene(root, 400, 300);

		primaryStage.setScene(scene);

		primaryStage.setTitle(TITLE);
		primaryStage.show();
	}
}
