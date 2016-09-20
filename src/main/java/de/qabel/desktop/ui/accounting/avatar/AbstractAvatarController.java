package de.qabel.desktop.ui.accounting.avatar;

import de.qabel.desktop.ui.AbstractController;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;

import javax.inject.Inject;
import java.net.URL;
import java.util.ResourceBundle;

abstract public class AbstractAvatarController extends AbstractController {

    abstract protected Label getAvatar();

    abstract protected String getAlias();

    public void generateAvatar(String alias) {
        getAvatar().setText(alias.substring(0, 1).toUpperCase());
        int hue = calculateHueFromAlias();
        String textColor = calculateForegroundColor(hue);
        String backgroundStyle = "-fx-background-color: hsb(" + hue + ","+getSaturation()+"%,100%);";
        String foregroundStyle = " -fx-text-fill: " + textColor + ";";
        getAvatar().setStyle(getAvatar().getStyle() + ";" + backgroundStyle + foregroundStyle);
    }

    protected int getSaturation() {
        return 100;
    }

    private int calculateHueFromAlias() {
        int avatarIndex = calculateAvatarIndex(getAlias());
        if (avatarIndex < 0)
            avatarIndex = avatarIndex * -1;
        return avatarIndex % 360;
    }

    private String calculateForegroundColor(int hue) {
        String textColor = "white";
        if (hue > 30 && hue < 190) {
            textColor = "#222222";
        }
        return textColor;
    }

    private int calculateAvatarIndex(String alias) {
        return Math.abs(alias.hashCode()) * (int) (Math.PI * 1000);
    }
}
