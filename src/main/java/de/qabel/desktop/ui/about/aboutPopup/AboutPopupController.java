package de.qabel.desktop.ui.about.aboutPopup;

import de.qabel.desktop.ui.AbstractController;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.TextArea;
import javafx.scene.layout.Pane;

import java.net.URL;
import java.util.ResourceBundle;

public class AboutPopupController extends AbstractController implements Initializable {

    @FXML
    private TextArea textAreaPopup;

    @FXML
    public Pane aboutPopup;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
    }

    public void btnClosePopup() {
        hidePopup();
    }

    public void setTextAreaContent (String content) {
        textAreaPopup.setText(content);
    }

    public void hidePopup() {
        aboutPopup.setVisible(false);
    }

    public void showPopup() {
        aboutPopup.setVisible(true);
    }
}
