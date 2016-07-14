package de.qabel.desktop.ui.accounting.qrcode;

import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;
import de.qabel.core.config.Identity;
import de.qabel.core.config.factory.DropUrlGenerator;
import de.qabel.desktop.config.ClientConfig;
import de.qabel.desktop.ui.AbstractController;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;
import net.glxn.qrgen.core.image.ImageType;
import net.glxn.qrgen.javase.QRCode;

import javax.inject.Inject;
import java.io.ByteArrayInputStream;

import java.net.URI;
import java.net.URL;
import java.util.ResourceBundle;

public class QRCodeController extends AbstractController implements Initializable {
    @FXML
    public Pane qrcode;

    @FXML
    private Label labelClient;

    @FXML
    private Label labelUrl;

    @FXML
    private Label labelKey;

    @FXML
    private ImageView imageQrCode;

    @Inject
    private Pane layoutWindow;

    @Inject
    private ClientConfig clientConfiguration;

    @Inject
    private DropUrlGenerator dropUrlGenerator;

    @Inject
    private URI accountingUri;

    @Inject
    private Identity identity;

    private String dropUrl;

    private String textQRCode;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
    }

    public void buttonClosePopup() {
        hidePopup();
    }

    public void showPopup() {
        setIdentity();
        qrcode.setVisible(true);
    }

    private void hidePopup() {
        qrcode.setVisible(false);
        layoutWindow.getChildren().remove(this);
    }

    public void setIdentity() {
        dropUrl = dropUrlGenerator.generateUrl().getUri().toString();
        textQRCode = "QABELCONTACT\n"
            + identity.getAlias() + "\n"
            + dropUrl + "\n"
            + identity.getEcPublicKey().getReadableKeyIdentifier();

        labelClient.setText(identity.getAlias());
        labelUrl.setText(dropUrl);
        labelKey.setText(identity.getEcPublicKey().getReadableKeyIdentifier());
        generateQRCode(textQRCode);
    }

    private void generateQRCode(String qrcode) {
        final byte[] imageBytes;
        QRCode.from(qrcode).withErrorCorrection(ErrorCorrectionLevel.L);
        imageBytes = QRCode.from(qrcode).withSize(300, 250).to(ImageType.PNG).stream().toByteArray();
        Image qrCodeGraphics = new Image(new ByteArrayInputStream(imageBytes));
        imageQrCode.setImage(qrCodeGraphics);
    }

    public String getDropUrl() {
        return dropUrl;
    }

    public String getPublicKey() {
        return identity.getEcPublicKey().getReadableKeyIdentifier();
    }

    public String getTextQRCode() {
        return textQRCode;
    }

    public String getAlias() {
        return identity.getAlias();
    }

}
