package de.qabel.desktop.ui.accounting.avatar;

import de.qabel.desktop.ui.AbstractController;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;

import javax.inject.Inject;
import java.net.URL;
import java.util.ResourceBundle;

public class AvatarController extends AbstractController implements Initializable {
    @FXML
    private Label avatar;

    @Inject
    private String alias;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        avatar.setText(alias.substring(0, 1).toUpperCase());

        int hue = calculateHueFromAlias();
        String textColor = calculateForegroundColor(hue);
        String backgroundStyle = "-fx-background-color: hsb(" + hue + ",100%,100%);";
        String foregroundStyle = " -fx-text-fill: " + textColor + ";";

        avatar.setStyle(avatar.getStyle() + ";" + backgroundStyle + foregroundStyle);
    }

    private int calculateHueFromAlias() {
        int avatarIndex = calculateAvatarIndex(alias);
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
