package de.qabel.desktop.ui.util;

import com.airhacks.afterburner.views.QabelFXMLView;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;

/**
 * Popup-like card inside the main window / given parent.
 */
public class Popup {
    private Pane parent;
    private StackPane root;
    private Parent content;

    private Integer height;
    private Integer width;

    /**
     * @TODO shrink popup to contents width size
     */
    public Popup(Pane parent, Parent content) {
        this(parent, content, null, null);
    }

    /**
     * auto bind height to contents preferred height
     *
     * @param parent
     * @param content
     * @param width
     */
    public Popup(Pane parent, Parent content, Integer width) {
        this(parent, content, width, null);
    }

    public Popup(Pane parent, Parent content, Integer width, Integer height) {
        this.parent = parent;
        this.content = content;
        this.width = width;
        this.height = height;

        construct();
    }

    private void construct() {
        Label bottomFader = new Label();
        bottomFader.getStyleClass().add("fade-bottom");
        bottomFader.setAlignment(Pos.BOTTOM_LEFT);

        Label topFader = new Label();
        topFader.getStyleClass().add("fade-top");
        topFader.setAlignment(Pos.TOP_LEFT);

        Button closeButton = new Button("X");
        closeButton.setId("buttonClosePopup");
        closeButton.getStyleClass().add("button-close-stackpane");
        closeButton.setOnAction(e -> close());

        StackPane container = new StackPane(content, bottomFader, topFader, closeButton);
        container.setAlignment(Pos.TOP_RIGHT);
        container.getStyleClass().add("popup-container");

        root = new StackPane(container);
        root.getStylesheets().add(QabelFXMLView.getGlobalStyleSheet());
        root.getStyleClass().add("vbox-popup");
        root.getStyleClass().add("bound-popup");
        root.setPickOnBounds(false);

        styleContainer(container);
    }

    private void styleContainer(StackPane container) {
        StringBuilder styleBuilder = new StringBuilder();
        if (width != null) {
            styleBuilder
                .append("-fx-max-width: ").append(width + 45).append("px;")
                .append("-fx-pref-width: ").append(width + 45).append("px;");
        }
        if (height != null) {
            styleBuilder
                .append("-fx-pref-height: ").append(height + 30).append("px;")
                .append("-fx-max-height: ").append(height + 30).append("px;");
        }

        String styles = styleBuilder.toString();
        container.setStyle(styles);
        content.setStyle(styles);

        ((Region)content).autosize();
        if (content instanceof Region) {
            ((Region) content).setPadding(new Insets(15, 30, 15, 15));
            if (height == null) {
                container.prefHeightProperty().bind(((Region) content).prefHeightProperty());
                container.maxHeightProperty().bind(((Region) content).prefHeightProperty());
            }
            if (width == null) {
                container.prefWidthProperty().bind(((Region) content).prefWidthProperty());
                container.maxWidthProperty().bind(((Region) content).prefWidthProperty());
            }
        }
    }

    public void close() {
        root.setVisible(false);
        parent.getChildren().remove(root);
    }

    public void show() {
        parent.getChildren().add(root);
        root.setVisible(true);
        root.toFront();
    }
}
