package de.qabel.desktop.ui.about;

import de.qabel.desktop.config.FilesAbout;
import de.qabel.desktop.ui.AbstractController;
import de.qabel.desktop.ui.about.aboutPopup.AboutPopupController;
import de.qabel.desktop.ui.about.aboutPopup.AboutPopupView;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;

import javafx.scene.control.Label;
import javafx.scene.layout.*;

import javax.inject.Inject;
import java.net.URL;
import java.util.*;

public class AboutController extends AbstractController implements Initializable {

    @FXML
    private Pane linkContainer;

    @FXML
    private Label appVersion;

    @FXML
    private Pane QAPLBox;

    @FXML
    private Pane thanksBox;

    @FXML
    private Pane termsOfServiceBox;

    @FXML
    private Pane imprintBox;

    @FXML
    private Pane privacyNotesBox;

    @FXML
    private Pane apacheLicenseBox;

    @FXML
    private Pane silLicenseBox;

    @FXML
    private Pane attributionLicenseBox;

    @FXML
    private Pane lgplLicenseBox;

    @FXML
    private Pane creativeLicenseBox;

    @Inject
    private Pane layoutWindow;

    @Inject
    private String currentVersion;

    @Inject
    private FilesAbout aboutFilesContent;

    public AboutPopupController popupController;
    private AboutPopupView popupView;

    private ResourceBundle resourceBundle;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        resourceBundle = resources;
        try {
            activateButtons();
            initializePopup();
            setDesktopVersion();
        } catch (Exception e) {
            alert("failed to load about contents: " + e.getMessage(), e);
        }
    }

    private void setDesktopVersion(){
        appVersion.setText(appVersion.getText() + " " + currentVersion);

    }
    private void activateButtons() {
        if (aboutFilesContent.QAPLContent.isEmpty()) {
            QAPLBox.setDisable(true);
        }
        if (aboutFilesContent.imprintContent.isEmpty()) {
            imprintBox.setDisable(true);
        }
        if (aboutFilesContent.silLicenseContent.isEmpty()) {
            silLicenseBox.setDisable(true);
        }
        if (aboutFilesContent.thanksFileContent.isEmpty()) {
            thanksBox.setDisable(true);
        }
        if (aboutFilesContent.apacheLicenseContent.isEmpty()) {
            apacheLicenseBox.setDisable(true);
        }
        if (aboutFilesContent.attributionLicenseContent.isEmpty()) {
            attributionLicenseBox.setDisable(true);
        }
        if (aboutFilesContent.creativeLicenseContent.isEmpty()) {
            creativeLicenseBox.setDisable(true);
        }
        if (aboutFilesContent.lgplLicenseContent.isEmpty()) {
            lgplLicenseBox.setDisable(true);
        }
        if(aboutFilesContent.privateNotesContent.isEmpty()) {
            privacyNotesBox.setDisable(true);
        }
        if(aboutFilesContent.termsOfServiceContent.isEmpty()) {
            termsOfServiceBox.setDisable(true);
        }
    }

    private void initializePopup() {
        popupView = new AboutPopupView();
        popupView.getView(layoutWindow.getChildren()::add);
        popupController = (AboutPopupController) popupView.getPresenter();
    }

    public void openThanksPopUp() {
        popupController.showThanksPopup();
    }

    public void openQAPLPopUp() {
        popupController.showQAPLPopup();
    }

    public void openImprintPopUp() {
        popupController.showImprintPopup();
    }

    public void openTermsOfServicePopUp() {
        popupController.showTermsOfServicePopup();
    }

    public void openPrivacyPopUp() {
        popupController.showPrivacyNotesPopup();
    }

    public void openApacheLicensePopUp() {
        popupController.showApacheLicensePopup();
    }

    public void openSilPopUp() {
        popupController.showSilLicensePopup();
    }

    public void openAttributionLicensePopUp() {
        popupController.showAttributionLicensePopup();
    }

    public void openLGPLLicensePopUp() {
        popupController.showLgplLicensePopup();
    }

    public void openCreativeLicensePopUp() {
        popupController.showCreativeLicensePopup();
    }
}
