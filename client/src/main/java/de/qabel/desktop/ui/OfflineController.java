package de.qabel.desktop.ui;

import de.qabel.desktop.daemon.NetworkStatus;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.layout.Pane;

import javax.inject.Inject;
import java.net.URL;
import java.util.ResourceBundle;

public class OfflineController extends AbstractController implements Initializable {
    @Inject
    private NetworkStatus networkStatus;

    @FXML
    Pane offlineIndicator;

    private FxNetworkStatus fxNetworkStatus;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        fxNetworkStatus = new FxNetworkStatus(networkStatus);

        offlineIndicator.visibleProperty().bind(fxNetworkStatus.onlineProperty().not());
    }
}
