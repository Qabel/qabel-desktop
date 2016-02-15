package de.qabel.desktop.ui;

import com.airhacks.afterburner.views.FXMLView;
import de.qabel.core.config.Identity;
import de.qabel.desktop.config.ClientConfiguration;
import de.qabel.desktop.daemon.management.MonitoredTransferManager;
import de.qabel.desktop.daemon.management.Transaction;
import de.qabel.desktop.daemon.management.TransferManager;
import de.qabel.desktop.daemon.management.WindowedTransactionGroup;
import de.qabel.desktop.ui.accounting.AccountingView;
import de.qabel.desktop.ui.accounting.avatar.AvatarView;
import de.qabel.desktop.ui.actionlog.ActionlogView;
import de.qabel.desktop.ui.contact.ContactView;
import de.qabel.desktop.ui.invite.InviteView;
import de.qabel.desktop.ui.remotefs.RemoteFSView;
import de.qabel.desktop.ui.sync.SyncView;
import de.qabel.desktop.ui.transfer.FxProgressModel;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;

import javax.inject.Inject;
import java.net.URL;
import java.util.Observable;
import java.util.ResourceBundle;

import static de.qabel.desktop.daemon.management.Transaction.STATE.FAILED;
import static de.qabel.desktop.daemon.management.Transaction.STATE.FINISHED;
import static de.qabel.desktop.daemon.management.Transaction.STATE.SKIPPED;

public class LayoutController extends AbstractController implements Initializable {
	ResourceBundle resourceBundle;
	ActionlogView actionlogView;
	@FXML
	public Label alias;
	@FXML
	public Label mail;
	@FXML
	VBox navi;
	@FXML
	private VBox scrollContent;

	@FXML
	private BorderPane window;

	@FXML
	private HBox activeNavItem;

	@FXML
	private Pane avatarContainer;

	@FXML
	private Pane selectedIdentity;

	@FXML
	private ScrollPane scroll;

	@FXML
	private ProgressBar uploadProgress;

	@Inject
	private ClientConfiguration clientConfiguration;

	@Inject
	private TransferManager transferManager;
	private HBox browseNav;
	private HBox contactsNav;
	private HBox actionlogNav;
	private HBox syncNav;
	private HBox inviteNav;
	private HBox accountingNav;

	@Override
	public void initialize(URL location, ResourceBundle resources) {
		this.resourceBundle = resources;

		navi.getChildren().clear();
		AccountingView accountingView = new AccountingView();
		actionlogView =  new ActionlogView();
		accountingNav = createNavItem(resourceBundle.getString("identity"), accountingView);
		navi.getChildren().add(accountingNav);

		browseNav = createNavItem(resourceBundle.getString("browse"), new RemoteFSView());
		contactsNav = createNavItem(resourceBundle.getString("contacts"), new ContactView());
		actionlogNav = createNavItem(resourceBundle.getString("actionlog"), actionlogView);
		syncNav = createNavItem(resourceBundle.getString("sync"), new SyncView());
		inviteNav = createNavItem(resourceBundle.getString("invite"), new InviteView());

		navi.getChildren().add(browseNav);
		navi.getChildren().add(contactsNav);
		navi.getChildren().add(actionlogNav);
		navi.getChildren().add(syncNav);
		navi.getChildren().add(inviteNav);

		scrollContent.setFillWidth(true);


		if (clientConfiguration.getSelectedIdentity() == null) {
			accountingView.getView(scrollContent.getChildren()::setAll);
			setActiveNavItem(accountingNav);
		}

		updateIdentity();
		clientConfiguration.addObserver((o, arg) -> updateIdentity());

		uploadProgress.setProgress(0);
		uploadProgress.setDisable(true);

		WindowedTransactionGroup progress = new WindowedTransactionGroup();
		if (transferManager instanceof MonitoredTransferManager) {
			MonitoredTransferManager tm = (MonitoredTransferManager)transferManager;
			tm.onAdd(progress::add);
		}
		FxProgressModel progressModel = new FxProgressModel(progress);
		uploadProgress.progressProperty().bind(progressModel.progressProperty());
		progress.onProgress(() -> {
			if (progress.isEmpty()) {
				uploadProgress.setVisible(false);
			} else {
				uploadProgress.setVisible(true);
			}
		});
	}

	private String lastAlias;
	private Identity lastIdentity;

	private void updateIdentity() {
		Identity identity = clientConfiguration.getSelectedIdentity();

		browseNav.setManaged(identity != null);
		contactsNav.setManaged(identity != null);
		actionlogNav.setManaged(identity != null);
		syncNav.setManaged(identity != null);
		inviteNav.setManaged(identity != null);
		selectedIdentity.setVisible(identity != null);

		avatarContainer.setVisible(identity != null);
		if (identity == null) {
			return;
		}

		final String currentAlias = identity.getAlias();
		if (currentAlias.equals(lastAlias)) {
			return;
		}

		new AvatarView(e ->  currentAlias).getViewAsync(avatarContainer.getChildren()::setAll);
		alias.setText(currentAlias);
		lastAlias = currentAlias;

		if (clientConfiguration.getAccount() == null) {
			return;
		}
		mail.setText(clientConfiguration.getAccount().getUser());
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
