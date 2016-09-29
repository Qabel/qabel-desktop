package de.qabel.desktop.ui.accounting.identitycontextmenu;

import de.qabel.core.config.Identity;
import de.qabel.desktop.action.DeleteIdentityAction;
import de.qabel.desktop.ui.AbstractController;
import de.qabel.desktop.ui.accounting.identity.IdentityEditController;
import de.qabel.desktop.ui.accounting.identity.IdentityEditView;
import de.qabel.desktop.ui.accounting.interactor.ExportIdentityContactInteractor;
import de.qabel.desktop.ui.accounting.interactor.ExportIdentityInteractor;
import de.qabel.desktop.ui.accounting.qrcode.QRCodeController;
import de.qabel.desktop.ui.accounting.qrcode.QRCodeView;
import de.qabel.desktop.ui.contact.menu.ContactMenuController;
import de.qabel.desktop.ui.util.Popup;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Tooltip;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;

import javax.inject.Inject;
import java.net.URL;
import java.util.ResourceBundle;

public class IdentityContextMenuController extends AbstractController implements Initializable {
    private ResourceBundle resourceBundle;

    @Inject
    private Identity identity;

    @Inject
    Pane layoutWindow;

    @Inject
    private DeleteIdentityAction deleteIdentityAction;

    @Inject
    private ExportIdentityInteractor exportIdentityInteractor;

    @Inject
    private ExportIdentityContactInteractor exportIdentityContactInteractor;

    @FXML
    VBox contextMenu;

    @FXML
    Button editButton;
    @FXML
    Button removeButton;
    @FXML
    Button exportIdentityButton;
    @FXML
    Button exportContactButton;
    @FXML
    Button privateKeyButton;
    @FXML
    Button publicKeyQRButton;
    @FXML
    Button publicKeyEmailButton;

    private static ImageView editImageView = setImageView(loadImage("/img/pencil.png"));
    private static ImageView deleteImageView = setImageView(loadImage("/img/delete.png"));
    private static ImageView uploadImageView = setImageView(loadImage("/img/upload.png"));
    private static ImageView exportIdentityImageView = setImageView(loadImage("/img/export.png"));
    private static ImageView exportContactImageView = setImageView(loadImage("/img/export.png"));
    private static ImageView privateKeyImageView = setImageView(loadImage("/img/qrcode.png"));
    private static ImageView qrcodeImageView = setImageView(loadImage("/img/qrcode.png"));
    private static ImageView emailImageView = setImageView(loadImage("/img/email.png"));

    private QRCodeView qrcodeView;
    QRCodeController qrcodeController;

    IdentityEditView identityEditView;
    IdentityEditController identityEditController;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        resourceBundle = resources;
        createButtonsGraphics();
        createButtonsTooltip();
        eventHandlerOpenQRPopup();
    }

    private void eventHandlerOpenQRPopup() {
        publicKeyQRButton.setOnAction(event -> {
            closeMenu();
            openQRCode();
        });
    }

    private static Image loadImage(String resourcePath) {
        return new Image(ContactMenuController.class.getResourceAsStream(resourcePath), 32, 32, true, true);
    }

    private static ImageView setImageView(Image image) {
        return new ImageView(image);
    }

    private void createButtonsGraphics() {
        editButton.setGraphic(editImageView);
        removeButton.setGraphic(deleteImageView);
        exportIdentityButton.setGraphic(exportIdentityImageView);
        exportContactButton.setGraphic(exportContactImageView);
        privateKeyButton.setGraphic(privateKeyImageView);
        publicKeyQRButton.setGraphic(qrcodeImageView);
        publicKeyEmailButton.setGraphic(emailImageView);
    }

    private void createButtonsTooltip() {
        Tooltip.install(editButton, new Tooltip(resourceBundle.getString("editDetails")));
        Tooltip.install(removeButton, new Tooltip(resourceBundle.getString("removeIdentity")));
        Tooltip.install(exportIdentityButton, new Tooltip(resourceBundle.getString("exportIdentityQID")));
        Tooltip.install(privateKeyButton, new Tooltip(resourceBundle.getString("exportPrivateKeyQR")));
        Tooltip.install(publicKeyQRButton, new Tooltip(resourceBundle.getString("sharePublicKeyQR")));
        Tooltip.install(publicKeyEmailButton, new Tooltip(resourceBundle.getString("sharePublicKeyEmail")));
    }

    private void closeMenu() {
        closeHandler.run();
    }

    private void createQrCodePopup(Pane container) {
        if (qrcodeView == null) {
            qrcodeView = new QRCodeView(identity);
            container.getChildren().add(qrcodeView.getView());
            qrcodeController = qrcodeView.getPresenter();
        }
    }

    public void openQRCode() {
        createQrCodePopup(layoutWindow);
        qrcodeController.show();
        closeMenu();
    }

    public void editIdentity() {
        createIdentityEdit(layoutWindow);
        closeMenu();
    }

    @FXML
    public void delete() {
        tryOrAlert(() -> {
            String title = identity.getAlias();
            String text = getString(resourceBundle, "confirmIdentitiyDeletion", identity.getAlias());
            confirm(title, text, () -> {
                deleteIdentityAction.delete(identity);
                closeMenu();
            });
        });
    }

    void createIdentityEdit(Pane container) {
        identityEditView = new IdentityEditView(identity);
        identityEditView.getView(v -> {
            Popup popup = new Popup(container, v);
            popup.show();
            identityEditView.getPresenter().onFinish(popup::close);
        });
        identityEditController = identityEditView.getPresenter();
    }

    private Runnable closeHandler = () -> {};
    public void onClose(Runnable closeHandler) {
        this.closeHandler = closeHandler;
    }

    public void exportIdentity() {
        exportIdentityInteractor.export(identity, layoutWindow.getScene().getWindow())
            .subscribe(aBoolean -> {}, this::alert);
    }

    public void exportContact() {
        exportIdentityContactInteractor.export(identity, layoutWindow.getScene().getWindow())
            .subscribe(aBoolean -> {}, this::alert);
    }
}
