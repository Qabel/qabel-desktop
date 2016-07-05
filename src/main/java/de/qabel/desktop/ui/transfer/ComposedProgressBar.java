package de.qabel.desktop.ui.transfer;

import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.layout.*;

public class ComposedProgressBar extends StackPane {

    private ProgressBar totalProgress;
    private GridPane itemProgress;
    private Pane background;
    private Label syncStatusLabel;
    private Label itemStatusLabel;
    private ColumnConstraints itemProgressColumn;

    public ComposedProgressBar() {
        initElements();
    }

    private void initElements() {
        styleProperty().setValue("-fx-width: 100%");
        getStyleClass().add("composedProgress");
        getStyleClass().add("class");

        totalProgress = new ProgressBar(0.35);
        totalProgress.setId("progress");
        totalProgress.setMaxHeight(20.0);
        totalProgress.setPrefHeight(20.0);
        totalProgress.setPrefWidth(1000.0);
        getChildren().add(totalProgress);

        itemProgress = new GridPane();
        itemProgress.setPickOnBounds(false);
        itemProgress.setStyle("-fx-background-color: null");
        itemProgressColumn = new ColumnConstraints(0.0, 0.0, 0.0);
        itemProgressColumn.setHgrow(Priority.SOMETIMES);
        itemProgressColumn.setPercentWidth(0);
        itemProgress.getColumnConstraints().add(itemProgressColumn);
        ColumnConstraints spacerColumn = new ColumnConstraints(0.0, 0.0, 0.0);
        spacerColumn.setPrefWidth(0);
        itemProgress.getColumnConstraints().add(spacerColumn);
        RowConstraints rowConstraints = new RowConstraints();
        rowConstraints.setMinHeight(10.0);
        rowConstraints.setPrefHeight(30.0);
        rowConstraints.setVgrow(Priority.SOMETIMES);
        itemProgress.getRowConstraints().add(rowConstraints);

        background = new Pane();
        background.setStyle("-fx-background-color: rgba(0,0,0,0.1)");
        itemProgress.getChildren().add(background);
        getChildren().add(itemProgress);

        BorderPane labelPane = new BorderPane();
        syncStatusLabel = new Label("initializing");
        syncStatusLabel.setAlignment(Pos.CENTER_LEFT);
        labelPane.setCenter(syncStatusLabel);
        itemStatusLabel = new Label("");
        itemStatusLabel.setAlignment(Pos.CENTER_RIGHT);
        labelPane.setRight(itemStatusLabel);
        getChildren().add(labelPane);
    }


    public ProgressBar getTotalProgress() {
        return totalProgress;
    }

    public Label getSyncStatusLabel() {
        return syncStatusLabel;
    }

    public Label getItemStatusLabel() {
        return syncStatusLabel;
    }

    public ColumnConstraints getItemProgressColumn() {
        return itemProgressColumn;
    }
}
