package de.qabel.desktop.ui;

import com.airhacks.afterburner.views.FXMLView;
import de.qabel.core.config.Identity;
import de.qabel.desktop.config.ClientConfiguration;
import de.qabel.desktop.ui.accounting.AccountingView;
import de.qabel.desktop.ui.accounting.avatar.AvatarView;
import de.qabel.desktop.ui.actionlog.ActionlogView;
import de.qabel.desktop.ui.contact.ContactView;
import de.qabel.desktop.ui.invite.InviteView;
import de.qabel.desktop.ui.remotefs.RemoteFSView;
import de.qabel.desktop.ui.sync.SyncView;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;

import javax.inject.Inject;
import java.net.URL;
import java.util.ResourceBundle;

public class LayoutController extends AbstractController implements Initializable {
	ResourceBundle resourceBundle;
	ActionlogView actionlogView;
	@FXML
	public Label alias;
	@FXML
	public Label mail;
	@FXML
	private VBox navi;

	@FXML
	private VBox scrollContent;

	@FXML
	private BorderPane window;

	@FXML
	private HBox activeNavItem;

	@FXML
	private Pane avatarContainer;


	@FXML
	private ScrollPane scroll;

	@Inject
	private ClientConfiguration clientConfiguration;

	@Override
	public void initialize(URL location, ResourceBundle resources) {
		this.resourceBundle = resources;

		navi.getChildren().clear();
		AccountingView accountingView = new AccountingView();
		actionlogView =  new ActionlogView();
		navi.getChildren().add(createNavItem(resourceBundle.getString("identity"), accountingView));
		navi.getChildren().add(createNavItem(resourceBundle.getString("browse"), new RemoteFSView()));
		navi.getChildren().add(createNavItem(resourceBundle.getString("contacts"), new ContactView()));
		navi.getChildren().add(createNavItem(resourceBundle.getString("actionlog"),actionlogView));
		navi.getChildren().add(createNavItem(resourceBundle.getString("sync"), new SyncView()));
		navi.getChildren().add(createNavItem(resourceBundle.getString("invite"), new InviteView()));

		scrollContent.setFillWidth(true);


		if (clientConfiguration.getSelectedIdentity() == null) {
			accountingView.getView(scrollContent.getChildren()::setAll);
		}

		updateIdentity();
		clientConfiguration.addObserver((o, arg) -> updateIdentity());
	}



	private void updateIdentity() {
		Identity identity = clientConfiguration.getSelectedIdentity();
		if (identity == null) {
			return;
		}

		new AvatarView(e -> identity.getAlias()).getViewAsync(avatarContainer.getChildren()::setAll);
		mail.setText(clientConfiguration.getAccount().getUser());
		alias.setText(identity.getAlias());
	}

	private HBox createNavItem(String label, FXMLView view) {
		Button button = new Button(label);
		HBox naviItem = new HBox(button);
		button.setOnAction(e -> {
			try {
				view.getView(scrollContent.getChildren()::setAll);
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

	public Object getScroller() {
		return scroll;
	}
}
