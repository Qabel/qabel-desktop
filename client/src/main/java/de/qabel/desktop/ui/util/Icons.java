package de.qabel.desktop.ui.util;

import com.vdurmont.emoji.Emoji;
import de.codecentric.centerdevice.javafxsvg.SvgImageLoaderFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class Icons {
    public static final String INFO = "/img/information_white.png";
    public static final String BROWSE = "/img/folder_white.png";
    public static final String DOTS = "/svg/dots-vertical.svg";
    public static final String KEY = "/img/key_variant.png";
    public static final String KEY_INVERSE = "/img/key_variant_white.png";
    public static final String LINK = "/img/earth_white.png";
    public static final String BROWSE_INVERSE = "/svg/ic_folder_white_24px.svg";
    public static final String SHARE = "/svg/ic_share_black_24px.svg";
    public static final String DELETE = "/svg/delete.svg";
    public static final String FOLDER = "/svg/ic_folder_black_24px.svg";
    public static final String FILE = "/svg/ic_folder_black_24px.svg";
    private static final Map<String, ImageView> imageCache = Collections.synchronizedMap(new HashMap<>());
    private static final Map<Emoji, String> emojiCache = Collections.synchronizedMap(new HashMap<>());

    public static final int LARGE_ICON_WIDTH = 32;

    static {
        SvgImageLoaderFactory.install();
    }

    public static ImageView getIcon(String path) {
        return getIcon(path, LARGE_ICON_WIDTH);
    }

    public static ImageView getIcon(String path, Integer width) {
        ImageView imageView;
        synchronized (imageCache) {
            if (imageCache.containsKey(path)) {
                ImageView cachedView = imageCache.get(path);
                imageView = new ImageView(cachedView.getImage());
            } else {
                imageView = new ImageView(path);
                imageCache.put(path, imageView);
            }
        }
        return resize(width, imageView);
    }

    @NotNull
    protected static ImageView resize(Integer width, ImageView view) {
        view.setFitWidth(width);
        view.setPreserveRatio(true);
        view.setCache(true);
        view.setSmooth(false);
        return view;
    }

    public static ImageView iconFromImage(Image image, Integer width) {
        return resize(width, new ImageView(image));
    }

    public static Image getImage(String path) {
        return new Image(Icons.class.getResourceAsStream(path));
    }

    public static ImageView getIcon(Emoji emoji) {
        return getIcon(emoji, LARGE_ICON_WIDTH);
    }

    public static ImageView getIcon(Emoji emoji, int width) {
        synchronized (emojiCache) {
            if (!emojiCache.containsKey(emoji)) {
                String emojiCode = emoji.getHtmlHexadecimal().replace(";&#x", "_").replace(";", "").replace("&#x", "");
                String path = "/icon/emoji/emoji_" + emojiCode + (emoji.supportsFitzpatrick() ? "_1f3fb" : "") + ".png";
                emojiCache.put(emoji, path);
            }
        }

        return getIcon(emojiCache.get(emoji), width);
    }
}
