package de.qabel.desktop.ui.accounting.avatar;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;

import javax.inject.Inject;
import java.net.URL;
import java.util.ResourceBundle;

public class ContactAvatarController extends AbstractAvatarController implements Initializable {

    @FXML
    protected Label avatar;

    @Inject
    protected String alias;

    @Inject
    protected Boolean unknown;

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

    @Override
    protected int getSaturation() {
        return unknown ? 50 : super.getSaturation();
    }
}
