package de.qabel.desktop.ui.accounting.item;

import de.qabel.core.config.Account;
import de.qabel.core.config.Identity;
import de.qabel.desktop.config.ClientConfig;
import de.qabel.desktop.ui.AbstractController;
import de.qabel.desktop.ui.accounting.avatar.AvatarView;
import de.qabel.desktop.ui.accounting.identitycontextmenu.IdentityContextMenuController;
import de.qabel.desktop.ui.accounting.identitycontextmenu.IdentityContextMenuView;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;

import javax.inject.Inject;
import java.net.URL;
import java.util.ResourceBundle;

public class AccountingItemController extends AbstractController implements Initializable {

    @FXML
    HBox root;

    ResourceBundle resourceBundle;

    @FXML
    Label alias;

    @FXML
    Label mail;

    @FXML
    Pane avatarContainer;

    @FXML
    RadioButton selectedRadio;

    @FXML
    private Button contextMenu;

    @Inject
    private Identity identity;

    @Inject
    private ClientConfig clientConfiguration;

    @Inject
    Pane layoutWindow;

    private IdentityContextMenuView contextMenuView;
    IdentityContextMenuController contextMenuController;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        resourceBundle = resources;

        alias.textProperty().addListener((o, a, b) -> updateAvatar());
        alias.setText(identity.getAlias());

        if (clientConfiguration.hasAccount()) {
            Account account = clientConfiguration.getAccount();
            mail.setText(account.getUser());
        }

        identity.attach(() -> Platform.runLater(() -> {
            alias.setText(identity.getAlias());
        }));

        updateSelection();
        clientConfiguration.onSelectIdentity(i -> updateSelection());
    }

    private void updateAvatar() {
        new AvatarView(e -> identity.getAlias()).getViewAsync(avatarContainer.getChildren()::setAll);
    }

    private void updateSelection() {
        selectedRadio.setSelected(identity.equals(clientConfiguration.getSelectedIdentity()));
    }

    public Identity getIdentity() {
        return identity;
    }

    private void initializeMenu() {
        contextMenuView = new IdentityContextMenuView(generateInjection("identity", identity));
        contextMenuView.getView(layoutWindow.getChildren()::add);
        contextMenuController = (IdentityContextMenuController) contextMenuView.getPresenter();
    }


    @FXML
    public void openMenu(MouseEvent event) {
        initializeMenu();
        double x = event.getSceneX() + layoutWindow.getScene().getWindow().getX();
        double y = event.getSceneY() + layoutWindow.getScene().getWindow().getY()
            + contextMenu.getHeight();

        Platform.runLater(() -> contextMenuController.openMenu(x, y));
    }

    public void selectIdentity() {
        if (selectedRadio.isSelected()) {
            clientConfiguration.selectIdentity(identity);
        }
    }
}
