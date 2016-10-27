package de.qabel.desktop.ui.contact.item;

import de.qabel.core.config.Contact;
import de.qabel.core.repository.IdentityRepository;
import de.qabel.desktop.config.ClientConfig;
import de.qabel.desktop.ui.AbstractController;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;

import javax.inject.Inject;
import java.net.URL;
import java.util.ResourceBundle;

public class BlankItemController extends AbstractController implements Initializable {

    ResourceBundle resourceBundle;

    @FXML
    Label firstChar;

    @Inject
    Contact contact;
    @Inject
    private ClientConfig clientConfiguration;
    @Inject
    private IdentityRepository identityRepository;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        resourceBundle = resources;
        firstChar.setText(contact.getAlias().substring(0,1).toUpperCase());
    }
}
