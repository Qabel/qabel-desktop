package de.qabel.desktop;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;


public class DesktopClient extends Application {
	private static final String TITLE = "Qabel Desktop Client";

	public static void main(String[] args) throws Exception {
		launch(args);
	}

	@Override
	public void start(Stage primaryStage) throws Exception {
		Parent root = FXMLLoader.load(getClass().getResource("/fxml/hello_world.fxml"));
		Scene scene = new Scene(root, 800, 600);

		primaryStage.setTitle(TITLE);
		primaryStage.setScene(scene);
		primaryStage.show();
	}
}
