package de.qabel.desktop.ui.contact.index.result;

import de.qabel.core.config.Contact;
import de.qabel.desktop.ui.AbstractController;
import de.qabel.desktop.ui.accounting.avatar.AvatarView;
import de.qabel.desktop.ui.contact.AssignContactPopover;
import de.qabel.desktop.ui.util.Icons;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.Pane;

import javax.inject.Inject;
import java.net.URL;
import java.util.ResourceBundle;

import static de.qabel.desktop.ui.util.Icons.DOTS;

public class IndexSearchResultItemController extends AbstractController implements Initializable {
    @FXML
    public Label fieldLabel;
    @FXML
    public Label aliasLabel;
    @FXML
    public Label publicKeyLabel;
    @FXML
    public Pane avatarContainer;
    @FXML
    private Button assignButton;

    @Inject
    private Contact contact;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        new AvatarView(contact.getAlias()).place(avatarContainer);

        fieldLabel.setText(contact.getEmail());
        aliasLabel.setText(contact.getAlias());
        publicKeyLabel.setText(contact.getKeyIdentifier());

        assignButton.setGraphic(Icons.getIcon(DOTS));
    }

    AssignContactPopover assignContactPopover;
    @FXML
    public void assign() {
        if (assignContactPopover != null) {
            assignContactPopover.hide();
        }
        assignContactPopover = new AssignContactPopover(contact);
        assignContactPopover.show(assignButton);
    }
}
