package de.qabel.desktop.ui.util;

import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

public class Icons {
    public static final String INFO = "/img/information_white.png";
    public static final String BROWSE = "/img/folder_white.png";
    public static final String DOTS = "/img/dots_vertical.png";
    public static final String KEY = "/img/key_variant.png";
    public static final String KEY_INVERSE = "/img/key_variant_white.png";
    public static final String LINK = "/img/earth_white.png";

    public static final int LARGE_ICON_WIDTH = 32;

    public static ImageView getIcon(String path) {
        return getIcon(path, LARGE_ICON_WIDTH);
    }

    public static ImageView getIcon(String path, Integer width) {
        ImageView view = new ImageView(path);
        view.setFitWidth(width);
        view.setPreserveRatio(true);
        view.setCache(true);
        view.setSmooth(false);
        return view;
    }

    public static Image getImage(String path) {
        return new Image(Icons.class.getResourceAsStream(path));
    }
}
