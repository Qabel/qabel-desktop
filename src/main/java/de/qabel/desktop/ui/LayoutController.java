package de.qabel.desktop.ui;

import com.airhacks.afterburner.views.FXMLView;
import de.qabel.desktop.ui.accounting.AccountingView;
import javafx.application.HostServices;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import javax.inject.Inject;
import java.net.URL;
import java.util.ResourceBundle;

public class LayoutController extends AbstractController implements Initializable {
	@FXML
	private VBox navi;

	@FXML
	private VBox content;

	@Inject
	private BorderPane window;

	private HBox activeNavItem;

	@Override
	public void initialize(URL location, ResourceBundle resources) {
		navi.getChildren().clear();
		navi.getChildren().add(createNavItem("Identitäten", new AccountingView()));
		navi.getChildren().add(createNavItem("Browse", new AccountingView()));

		content.setFillWidth(true);
	}

	private HBox createNavItem(String label, FXMLView view) {
		Button button = new Button(label);
		HBox naviItem = new HBox(button);
		button.setOnAction(e -> {
			try {
				view.getView(content.getChildren()::setAll);
				setActiveNavItem(naviItem);
			} catch (Exception exception) {
				alert(exception.getMessage(), exception);
			}
		});
		naviItem.getStyleClass().add("navi-item");
		return naviItem;
	}

	private void setActiveNavItem(HBox naviItem) {
		if (activeNavItem != null) {
			activeNavItem.getStyleClass().remove("active");
		}
		naviItem.getStyleClass().add("active");
		activeNavItem = naviItem;
	}
}
