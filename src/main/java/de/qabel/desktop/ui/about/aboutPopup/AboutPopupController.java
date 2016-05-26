package de.qabel.desktop.ui.about.aboutPopup;

import de.qabel.desktop.ui.AbstractController;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.layout.Pane;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

public class AboutPopupController extends AbstractController implements Initializable {

    @FXML
    TextArea textAreaPopup;

    @FXML
    Pane aboutPopup;

    @FXML
    Button btnClosePopup;

    private Stage stage;
    private Double coordX;
    private Double coordY;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
    }

    public void btnClosePopup(ActionEvent actionEvent) throws IOException {
        getStage().close();
    }

    public void setTextAreaContent (String content){
        textAreaPopup.setText(content);
    }

    public void showPopup(){
        stage = getStage();
        stage.initStyle(StageStyle.TRANSPARENT);
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.setResizable(false);
        stage.setX(getCoordX());
        stage.setY(getCoordY());
        stage.showAndWait();
    }

    public void setStage(Stage stage) {
        this.stage = stage;
    }

    public Stage getStage() {
        return stage;
    }

    public void setCoordX(Double coordX) { this.coordX = coordX; }

    public void setCoordY(Double coordY) { this.coordY = coordY; }

    public Double getCoordX() {
        return coordX;
    }

    public Double getCoordY() {
        return coordY;
    }
}
