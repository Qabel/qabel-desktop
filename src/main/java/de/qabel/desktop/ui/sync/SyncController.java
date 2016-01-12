package de.qabel.desktop.ui.sync;

import de.qabel.desktop.config.BoxSyncConfig;
import de.qabel.desktop.config.ClientConfiguration;
import de.qabel.desktop.config.DefaultBoxSyncConfig;
import de.qabel.desktop.ui.AbstractController;
import de.qabel.desktop.ui.sync.item.SyncItemView;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.layout.VBox;

import javax.inject.Inject;
import java.net.URL;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.ResourceBundle;

public class SyncController extends AbstractController implements Initializable {
	@FXML
	private VBox syncItemContainer;

	ObservableList<Node> syncItemNodes;

	@Inject
	private ClientConfiguration clientConfiguration;

	@Override
	public void initialize(URL location, ResourceBundle resources) {
		syncItemNodes = syncItemContainer.getChildren();

		syncItemNodes.clear();
		for (BoxSyncConfig syncConfig : Collections.unmodifiableList(clientConfiguration.getBoxSyncConfigs())) {
			syncItemNodes.add(new SyncItemView(s -> syncConfig).getView());
		}
	}
}
