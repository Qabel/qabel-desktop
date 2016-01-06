package de.qabel.desktop.remoteFS;

import javafx.application.Application;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;


public class RemoteFSApplication extends Application {

    private static final String TITLE = "Qabel Desktop Client";

    public static void main(String[] args) throws Exception {
        Application.launch(RemoteFSApplication.class, args);
    }

    @Override
    public void start(Stage primaryStage) throws Exception {

        primaryStage.setTitle(TITLE);

        Parent root = FXMLLoader.load(getClass().getResource("/fxml/RemoteFSView.fxml"));
        final Scene scene = new Scene(root, 600, 600);
        primaryStage.setScene(scene);
        primaryStage.show();
    }


}

