package de.qabel.desktop.ui;

import com.airhacks.afterburner.views.FXMLView;
import de.qabel.core.config.Identity;
import de.qabel.desktop.config.ClientConfiguration;
import de.qabel.desktop.ui.accounting.AccountingView;
import de.qabel.desktop.ui.accounting.avatar.AvatarView;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;

import javax.inject.Inject;
import java.net.URL;
import java.util.ResourceBundle;

public class LayoutController extends AbstractController implements Initializable {
	@FXML
	public Label alias;
	@FXML
	public Label mail;
	@FXML
	private VBox navi;

	@FXML
	private VBox content;

	@FXML
	private BorderPane window;

	@FXML
	private HBox activeNavItem;

	@FXML
	private Pane avatarContainer;

	@Inject
	private ClientConfiguration clientConfiguration;

	@Override
	public void initialize(URL location, ResourceBundle resources) {
		navi.getChildren().clear();
		AccountingView accountingView = new AccountingView();
		navi.getChildren().add(createNavItem("Identitäten", accountingView));
		navi.getChildren().add(createNavItem("Browse", new AccountingView()));

		content.setFillWidth(true);

		if (clientConfiguration.getSelectedIdentity() == null) {
			accountingView.getView(content.getChildren()::setAll);
		}

		updateIdentity();
		clientConfiguration.addObserver((o, arg) -> updateIdentity());
	}

	private void updateIdentity() {
		Identity identity = clientConfiguration.getSelectedIdentity();
		if (identity == null) {
			return;
		}

		new AvatarView(e->identity.getAlias()).getViewAsync(avatarContainer.getChildren()::setAll);
		mail.setText(clientConfiguration.getAccount().getUser());
		alias.setText(identity.getAlias());
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
