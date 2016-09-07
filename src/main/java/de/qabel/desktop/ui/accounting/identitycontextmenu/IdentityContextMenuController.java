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
import java.util.ResourceBundle;

public class IdentityContextMenuController extends AbstractController implements Initializable {

    private ResourceBundle resourceBundle;

    @Inject
    private Identity identity;

    @Inject
    Pane layoutWindow;

    @FXML
    AnchorPane identityContextMenu;

    @FXML
    VBox contextMenu;

    private QRCodeView qrcodeView;
    QRCodeController qrcodeController;

    private IdentityEditView identityEditView;
    IdentityEditController identityEditController;
    PopOver popOver;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        resourceBundle = resources;
    }


    private void createPopOver() {
        popOver = new PopOver();
        popOver.setArrowLocation(PopOver.ArrowLocation.TOP_RIGHT);
        popOver.setAutoFix(true);
        popOver.setAutoHide(true);
        popOver.setHideOnEscape(true);
        popOver.setDetachable(false);
        popOver.setContentNode(contextMenu);
    }

    public void openMenu() {
        createPopOver();
        Platform.runLater(() -> {
            popOver.show(identityContextMenu);
            appendToLayout();
            clearLayout();
        });
    }

    public void openMenu(double coordPopOverX, double coordPopOverY) {
        Platform.runLater(() -> {
            popOver.show(identityContextMenu, coordPopOverX, coordPopOverY);
            appendToLayout();
            clearLayout();
        });
    }

    private void appendToLayout() {
        if (layoutWindow != null && !layoutWindow.getChildren().contains(identityContextMenu)) {
            layoutWindow.getChildren().add(identityContextMenu);
        }
    }

    public void closeMenu() {
        Platform.runLater(() -> {
            popOver.hide();
            clearLayout();
        });
    }

    private void clearLayout() {
        if (layoutWindow != null && layoutWindow.getChildren().contains(identityContextMenu)) {
            layoutWindow.getChildren().remove(identityContextMenu);
        }
    }

    private void createQrCodePopup(Pane container) {
        if (qrcodeView == null) {
            qrcodeView = new QRCodeView(generateInjection("identity", identity));
            qrcodeView.getView(container.getChildren()::add);
            qrcodeController = (QRCodeController) qrcodeView.getPresenter();
        }
    }

    public void openQRCode() {
        closeMenu();
        createQrCodePopup(layoutWindow);
        qrcodeController.showPopup();
    }

    public void openIdentityEdit() {
        closeMenu();
        createIdentityEdit(layoutWindow);
        identityEditController.show();
    }

    IdentityEditView createIdentityEdit(Pane container) {
        if (identityEditView == null) {
            identityEditView = new IdentityEditView(generateInjection("identity", identity));
            identityEditView.getView(container.getChildren()::add);
            identityEditController = (IdentityEditController) identityEditView.getPresenter();
        }
        return identityEditView;
    }
}
