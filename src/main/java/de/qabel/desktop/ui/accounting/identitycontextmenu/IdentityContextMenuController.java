package de.qabel.desktop.ui.accounting.identitycontextmenu;

import de.qabel.core.config.Identity;

import de.qabel.core.repository.IdentityRepository;
import de.qabel.core.repository.exception.PersistenceException;

import de.qabel.desktop.ui.AbstractController;
import de.qabel.desktop.ui.accounting.qrcode.QRCodeController;
import de.qabel.desktop.ui.accounting.qrcode.QRCodeView;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.TextInputDialog;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import org.controlsfx.control.PopOver;

import javax.inject.Inject;
import java.net.URL;
import java.util.*;

public class IdentityContextMenuController extends AbstractController implements Initializable {

    ResourceBundle resourceBundle;

    @Inject
    private Identity identity;

    @Inject
    private Pane layoutWindow;

    @FXML
    AnchorPane menuQR;

    @FXML
    VBox vboxMenu;

    @Inject
    private IdentityRepository identityRepository;

    private TextInputDialog dialog;

    private QRCodeView qrcodeView;
    private QRCodeController qrcodeController;
    private PopOver popOver;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        resourceBundle = resources;
    }

    private void initializeQRPopup() {
        if (qrcodeView == null) {
            final Map<String, Object> injectionContext = new HashMap<>();
            injectionContext.put("identity", identity);
            qrcodeView = new QRCodeView(injectionContext::get);
            qrcodeView.getView(layoutWindow.getChildren()::add);
            qrcodeController = (QRCodeController) qrcodeView.getPresenter();
        }
    }

    public void showMenu(double coordPopOverX, double coordPopOverY) {
        if (!layoutWindow.getChildren().contains(menuQR)) {
            layoutWindow.getChildren().add(menuQR);
        }

        initializePopOver();
        popOver.show(menuQR, coordPopOverX, coordPopOverY);
        Platform.runLater(() -> layoutWindow.getChildren().remove(menuQR));
    }

    private void initializePopOver() {
        popOver = new PopOver();
        popOver.setArrowLocation(PopOver.ArrowLocation.TOP_RIGHT);
        popOver.setContentNode(new VBox(vboxMenu));
        popOver.setAutoFix(true);
        popOver.setAutoHide(true);
        popOver.setHideOnEscape(true);
        popOver.setDetachable(false);
    }

    private void closeMenu() {
        popOver.hide();
        layoutWindow.getChildren().remove(menuQR);
    }

    public void openQRCode() {
        closeMenu();
        initializeQRPopup();
        Platform.runLater(() -> qrcodeController.showPopup());
    }

    public void editIdentity(MouseEvent event) {
        dialog = new TextInputDialog(identity.getAlias());
        dialog.setX(layoutWindow.getScene().getWindow().getX() + (event.getSceneX() * 2));
        dialog.setY(layoutWindow.getScene().getWindow().getY() + (event.getSceneY() * 4));
        dialog.setHeaderText(null);
        dialog.setTitle(resourceBundle.getString("accountingItemChangeAlias"));
        dialog.setContentText(resourceBundle.getString("accountingItemNewAlias"));
        Optional<String> result = dialog.showAndWait();
        result.ifPresent(this::setAlias);
    }

    public void setAlias(String alias) {
        identity.setAlias(alias);
        try {
            identityRepository.save(identity);
        } catch (PersistenceException e) {
            alert("Failed to save identity", e);
        }
    }

    public String getAlias() {
        return identity.getAlias();
    }
}
