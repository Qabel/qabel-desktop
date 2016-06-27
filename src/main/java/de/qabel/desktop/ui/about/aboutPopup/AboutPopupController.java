package de.qabel.desktop.ui.about.aboutPopup;

import de.qabel.desktop.config.FilesAbout;
import de.qabel.desktop.ui.AbstractController;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.TextArea;
import javafx.scene.layout.Pane;

import javax.inject.Inject;
import java.net.URL;
import java.util.ResourceBundle;

public class AboutPopupController extends AbstractController implements Initializable {

    @FXML
    private TextArea textAreaPopup;

    @FXML
    public Pane aboutPopup;

    @Inject
    private FilesAbout aboutFilesContent;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
    }

    public void buttonClosePopup() {
        hidePopup();
    }

    public void showQAPLPopup() {
        showPopup(aboutFilesContent.QAPLContent);
    }

    public void showThanksPopup() {
        showPopupTextAlignmentCenter(aboutFilesContent.thanksFileContent);
    }

    public void showImprintPopup() {
        showPopup(aboutFilesContent.imprintContent);
    }

    public void showTermsOfServicePopup() {
        showPopup(aboutFilesContent.termsOfServiceContent);
    }

    public void showPrivacyNotesPopup() {
        showPopup(aboutFilesContent.privateNotesContent);
    }

    public void showApacheLicensePopup() {
        showPopup(aboutFilesContent.apacheLicenseContent);
    }

    public void showSilLicensePopup() {
        showPopup(aboutFilesContent.silLicenseContent);
    }

    public void showLgplLicensePopup() {
        showPopup(aboutFilesContent.lgplLicenseContent);
    }

    public void showCreativeLicensePopup() {
        showPopup(aboutFilesContent.creativeLicenseContent);
    }

    public void showAttributionLicensePopup() {
        showPopup(aboutFilesContent.attributionLicenseContent);
    }

    public void showEuropeanLicensePopup() {
        showPopup(aboutFilesContent.europeanLicenseContent);
    }

    public void showBouncyLicensePopup() {
        showPopup(aboutFilesContent.bouncyLicenseContent);
    }

    public void showEclipseLicensePopup() {
        showPopup(aboutFilesContent.eclipseLicenseContent);
    }

    public void showJsonLicensePopup() {
        showPopup(aboutFilesContent.jsonLicenseContent);
    }

    public void showBSDLicensePopup() {
        showPopup(aboutFilesContent.bsdLicenseContent);

    }

    public void showMITLicensePopup() {
        showPopup(aboutFilesContent.mitLicenseContent);
    }

    public void showInnoLicensePopup() {
        showPopup(aboutFilesContent.innoLicenseContent);
    }

    public void showLaunch4JLicensePopup() {
        showPopup(aboutFilesContent.launch4jLicenseContent);
    }

    public void setTextAreaContent(String content) {
        textAreaPopup.setText(content);
    }

    public String getTextAreaContent() {
        return textAreaPopup.getText();
    }

    private void hidePopup() {
        aboutPopup.setVisible(false);
    }

    public void showPopup(String content) {
        setStyleTextArea();
        setTextAreaContent(content);
        aboutPopup.setVisible(true);
    }

    private void showPopupTextAlignmentCenter(String content) {
        setStyleTextAreaCenter();
        setTextAreaContent(content);
        aboutPopup.setVisible(true);
    }

    private void setStyleTextArea() {
        textAreaPopup.getStyleClass().clear();
        textAreaPopup.getStyleClass().add("text-area-popup");
    }

    private void setStyleTextAreaCenter() {
        textAreaPopup.getStyleClass().clear();
        textAreaPopup.getStyleClass().add("align-text-center");
    }
}
