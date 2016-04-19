package de.qabel.desktop.ui;

import com.airhacks.afterburner.views.FXMLView;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.Button;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;

public class NaviItem extends HBox {
    public static final String ACTIVE_CLASS = "active";
    private Button button;
    private StringProperty labelProperty;
    private FXMLView view;
    private Indicator indicator = new Indicator();

    public NaviItem(String label, FXMLView view) {
        labelProperty = new SimpleStringProperty(label);
        this.view = view;

        button = new Button();
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
        } else {
            getStyleClass().remove(ACTIVE_CLASS);
        }
    }

    public Indicator getIndicator() {
        return indicator;
    }
}
