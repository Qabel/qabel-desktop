package de.qabel.desktop.ui.accounting.item;

import de.qabel.core.config.Account;
import de.qabel.core.config.Identity;
import de.qabel.desktop.config.ClientConfig;
import de.qabel.desktop.ui.AbstractController;
import de.qabel.desktop.ui.accounting.avatar.AvatarView;
import de.qabel.desktop.ui.accounting.identitycontextmenu.IdentityContextMenuView;
import de.qabel.desktop.ui.accounting.identitycontextmenu.IdentityContextMenuController;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import org.controlsfx.control.PopOver;

import javax.inject.Inject;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.ResourceBundle;

public class AccountingItemController extends AbstractController implements Initializable {

    ResourceBundle resourceBundle;

    @FXML
    HBox root;

    @FXML
    Label alias;

    @FXML
    Label mail;

    @FXML
    Pane avatarContainer;

    @FXML
    RadioButton selectedRadio;

    @Inject
    private Identity identity;

    @Inject
    private ClientConfig clientConfiguration;

    public PopOver popOver;

    public IdentityContextMenuView identityMenuView;
    public IdentityContextMenuController identityMenuController;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        resourceBundle = resources;

        alias.textProperty().addListener((o, a, b) -> updateAvatar());
        alias.setText(identity.getAlias());

        if (clientConfiguration.hasAccount()) {
            Account account = clientConfiguration.getAccount();
            mail.setText(account.getUser());
        }

        identity.attach(() -> Platform.runLater(() -> alias.setText(identity.getAlias())));

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

    private void initializeMenu(double coordX, double coordY) {
        final Map<String, Object> injectionContext = new HashMap<>();
        injectionContext.put("identity", identity);
        identityMenuView = new IdentityContextMenuView(injectionContext::get);
        identityMenuView.getView(view -> {
            popOver = new PopOver();
            popOver.setArrowLocation(PopOver.ArrowLocation.TOP_RIGHT);
            popOver.setContentNode(new VBox(identityMenuController.contextMenu));
            popOver.show(root, coordX, coordY);
        });
        identityMenuController = (IdentityContextMenuController) identityMenuView.getPresenter();
    }

    public void openMenuQR(MouseEvent event) {
        initializeMenu(event.getScreenX(), event.getScreenY());
        Platform.runLater(() -> identityMenuController.openMenu());
    }

    public void selectIdentity() {
        if (selectedRadio.isSelected()) {
            clientConfiguration.selectIdentity(identity);
        }
    }
}
