package de.qabel.desktop.ui.accounting.avatar;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;

import javax.inject.Inject;
import java.net.URL;
import java.util.ResourceBundle;

public class AvatarController extends AbstractAvatarController implements Initializable {

    @FXML
    protected Label avatar;
    @Inject
    protected String alias;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        generateAvatar(alias);
    }

    @Override
    protected Label getAvatar() {
        return avatar;
    }

    @Override
    protected String getAlias() {
        return alias;
    }
}
