package de.qabel.desktop.ui;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;

public class NaviItem extends HBox {
    public static final String ACTIVE_CLASS = "active";
    public Button button;
    private StringProperty labelProperty;
    private Indicator indicator = new Indicator();

    public NaviItem(String label, Image image) {
        this(label, new ImageView(image));
    }

    public NaviItem(String label, ImageView imageView) {
        labelProperty = new SimpleStringProperty(label);
        button = new Button();
        button.setGraphic(imageView);
        button.textProperty().bindBidirectional(labelProperty);
        getChildren().add(button);
        indicator.setVisible(false);
        getStyleClass().add("navi-item");
        BorderPane statusPane = new BorderPane();
        statusPane.setCenter(indicator);
        getChildren().add(statusPane);
    }

    public void setOnAction(EventHandler<ActionEvent> handler) {
        button.setOnAction(handler);
    }

    public void setActive(boolean active) {
        if (active) {
            getStyleClass().add(ACTIVE_CLASS);
            button.getGraphic().getStyleClass().add("darkgrey");
        } else {
            getStyleClass().remove(ACTIVE_CLASS);
            button.getGraphic().getStyleClass().remove("darkgrey");
        }
    }

    public Indicator getIndicator() {
        return indicator;
    }
}
