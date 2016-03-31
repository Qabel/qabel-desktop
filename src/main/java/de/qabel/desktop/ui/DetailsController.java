package de.qabel.desktop.ui;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;

import java.net.URL;
import java.util.ResourceBundle;

public class DetailsController extends AbstractController implements Initializable {
    @FXML
    Pane detailsPane;

    @FXML
    Label closeDetails;

    @FXML
    Pane detailsContainer;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        closeDetails.addEventHandler(MouseEvent.MOUSE_CLICKED, event -> detailsPane.setVisible(false));
    }

    public void hide() {
        Platform.runLater(() -> detailsPane.setVisible(false));
    }

    public void show() {
        Platform.runLater(() -> detailsPane.setVisible(true));
    }

    public void show(Node content) {
        Platform.runLater(() -> {
            detailsContainer.getChildren().setAll(content);
            detailsPane.setVisible(true);
        });
    }
}
