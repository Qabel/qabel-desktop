package de.qabel.desktop.ui.util;

import de.codecentric.centerdevice.javafxsvg.SvgImageLoaderFactory;
import javafx.scene.Node;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.SVGPath;
import org.apache.commons.io.IOUtils;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.io.InputStream;

public class Icons {
    public static final String INFO = "/img/information_white.png";
    public static final String BROWSE = "/img/folder_white.png";
    public static final String DOTS = "/img/dots_vertical.png";
    public static final String KEY = "/img/key_variant.png";
    public static final String KEY_INVERSE = "/img/key_variant_white.png";
    public static final String LINK = "/img/earth_white.png";
    public static final String BROWSE_INVERSE = "/svg/ic_folder_white_24px.svg";
    public static final String SHARE = "/svg/ic_share_black_24px.svg";

    public static final int LARGE_ICON_WIDTH = 32;

    static {
        SvgImageLoaderFactory.install();
    }

    public static ImageView getIcon(String path) {
        return getIcon(path, LARGE_ICON_WIDTH);
    }

    public static ImageView getIcon(String path, Integer width) {
        return resize(width, new ImageView(path));
    }

    @NotNull
    protected static ImageView resize(Integer width, ImageView view) {
        view.setFitWidth(width);
        view.setPreserveRatio(true);
        view.setCache(true);
        view.setSmooth(true);
        return view;
    }

    public static ImageView iconFromImage(Image image, Integer width) {
        return resize(width, new ImageView(image));
    }

    public static Node getSvg(String path, Integer width) {
        InputStream resource = Icons.class.getResourceAsStream(path);
        if (resource == null) {
            throw new IllegalArgumentException("no file at " + path);
        }
        try {
            SVGPath shape = new SVGPath();
            String value = IOUtils.toString(resource);
            value = value.substring(value.indexOf(">") + 1);
            shape.setContent(value);
            shape.setFill(Color.RED);
            shape.setStroke(Color.BLUE);
            Pane image = new Pane();
            image.setShape(shape);
            image.setMinSize(width, width);
            image.setPrefSize(width, width);
            image.setMaxSize(width, width);
            image.setStyle("-fx-background-color: blue; -fx-text-fill: green; -fx-fill: yellow;");
            return image;
        } catch (IOException e) {
            throw new IllegalStateException("failed to load image " + path, e);
        } finally {
            try {
                resource.close();
            } catch (IOException ignored) {}
        }
    }

    public static Image getImage(String path) {
        return new Image(Icons.class.getResourceAsStream(path));
    }
}
