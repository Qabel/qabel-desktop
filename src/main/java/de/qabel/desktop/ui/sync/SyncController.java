package de.qabel.desktop.ui.sync;

import de.qabel.desktop.config.BoxSyncConfig;
import de.qabel.desktop.config.ClientConfiguration;
import de.qabel.desktop.ui.AbstractController;
import de.qabel.desktop.ui.sync.item.DummySyncItemView;
import de.qabel.desktop.ui.sync.item.SyncItemView;
import de.qabel.desktop.ui.sync.setup.SyncSetupController;
import de.qabel.desktop.ui.sync.setup.SyncSetupView;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import javax.inject.Inject;
import java.net.URL;
import java.util.Collections;
import java.util.ResourceBundle;

public class SyncController extends AbstractController implements Initializable {
	@FXML
	private VBox syncItemContainer;

	ObservableList<Node> syncItemNodes;

	Stage addStage;

	@Inject
	private ClientConfiguration clientConfiguration;

	SyncSetupController syncSetupController;

	@Override
	public void initialize(URL location, ResourceBundle resources) {
		syncItemNodes = syncItemContainer.getChildren();

		ObservableList<BoxSyncConfig> boxSyncConfigs = reload();
		boxSyncConfigs.addListener((ListChangeListener<BoxSyncConfig>) c -> reload());
	}

	private ObservableList<BoxSyncConfig> reload() {
		syncItemNodes.clear();
		ObservableList<BoxSyncConfig> boxSyncConfigs = clientConfiguration.getBoxSyncConfigs();

		if(Collections.unmodifiableList(boxSyncConfigs).size() == 0){
			syncItemNodes.add(new DummySyncItemView().getView());
			return boxSyncConfigs;
		}
		for (BoxSyncConfig syncConfig : Collections.unmodifiableList(boxSyncConfigs)) {
			syncItemNodes.add(new SyncItemView(s -> s.equals("syncConfig") ? syncConfig : null).getView());
		}
		return boxSyncConfigs;
	}

	public void addSync(ActionEvent actionEvent) {
		addStage = new Stage();
		SyncSetupView view = new SyncSetupView();
		Scene scene = new Scene(view.getView());
		addStage.setScene(scene);
		syncSetupController = (SyncSetupController) view.getPresenter();
		syncSetupController.setStage(addStage);
		addStage.show();
	}
}
