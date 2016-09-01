package de.qabel.desktop.ui.accounting.identitycontextmenu;

import de.qabel.core.config.Identity;
import de.qabel.desktop.ui.AbstractController;
import de.qabel.desktop.ui.accounting.identity.IdentityEditController;
import de.qabel.desktop.ui.accounting.identity.IdentityEditView;
import de.qabel.desktop.ui.accounting.qrcode.QRCodeController;
import de.qabel.desktop.ui.accounting.qrcode.QRCodeView;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import org.controlsfx.control.PopOver;

import javax.inject.Inject;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.ResourceBundle;

public class IdentityContextMenuController extends AbstractController implements Initializable {

    ResourceBundle resourceBundle;

    @Inject
    private Identity identity;

    @Inject
    Pane layoutWindow;

    @FXML
    AnchorPane identityContextMenu;

    @FXML
    VBox vboxMenu;

    private QRCodeView qrcodeView;
    private QRCodeController qrcodeController;

    private IdentityEditView identityEditView;
    IdentityEditController identityEditController;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        resourceBundle = resources;
    }

    private void initializeQRPopup(Pane container) {
        if (qrcodeView == null) {
            final Map<String, Object> injectionContext = new HashMap<>();
            injectionContext.put("identity", identity);
            qrcodeView = new QRCodeView(injectionContext::get);
            qrcodeView.getView(container.getChildren()::add);
            qrcodeController = (QRCodeController) qrcodeView.getPresenter();
        }
    }

    public void showMenu(double coordPopOverX, double coordPopOverY) {
        PopOver popOver = createPopOver();
        popOver.show(identityContextMenu, coordPopOverX, coordPopOverY);
    }

    public void showMenu() {
        PopOver popOver = createPopOver();
        popOver.show(identityContextMenu);
    }

    private PopOver createPopOver() {
        PopOver popOver = new PopOver();
        popOver.setArrowLocation(PopOver.ArrowLocation.TOP_RIGHT);
        popOver.setContentNode(new VBox(vboxMenu));
        popOver.setAutoFix(true);
        popOver.setAutoHide(true);
        popOver.setHideOnEscape(true);
        popOver.setDetachable(false);
        return popOver;
    }

    private void closeMenu() {
        Platform.runLater(() -> {
            layoutWindow.getChildren().remove(identityContextMenu);
        });
    }

    public void openQRCode() {
        closeMenu();
        initializeQRPopup(layoutWindow);
        Platform.runLater(() -> qrcodeController.showPopup());
    }

    @FXML
    void openIdentityEdit() {
        identityContextMenu.getChildren().clear();
        createIdentityEdit(identityContextMenu);
        Platform.runLater(() -> identityEditController.show());
    }

    IdentityEditView createIdentityEdit(Pane container) {
        if (identityEditView == null) {
            identityEditView = new IdentityEditView(generateInjection("identity", identity));
            identityEditView.getView(container.getChildren()::add);
            identityEditController = (IdentityEditController) identityEditView.getPresenter();
        }
        return identityEditView;
    }

    public void setAlias(String alias) {
        identityEditController.setAlias(alias);
    }
}
