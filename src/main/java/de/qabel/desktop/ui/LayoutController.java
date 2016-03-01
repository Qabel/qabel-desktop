package de.qabel.desktop.ui;

import com.airhacks.afterburner.views.FXMLView;
import de.qabel.core.config.Identity;
import de.qabel.desktop.config.ClientConfiguration;
import de.qabel.desktop.daemon.management.MonitoredTransferManager;
import de.qabel.desktop.daemon.management.TransferManager;
import de.qabel.desktop.daemon.management.WindowedTransactionGroup;
import de.qabel.desktop.ui.accounting.AccountingView;
import de.qabel.desktop.ui.accounting.avatar.AvatarView;
import de.qabel.desktop.ui.actionlog.ActionlogView;
import de.qabel.desktop.ui.contact.ContactView;
import de.qabel.desktop.ui.feedback.FeedbackView;
import de.qabel.desktop.ui.invite.InviteView;
import de.qabel.desktop.ui.remotefs.RemoteFSView;
import de.qabel.desktop.ui.sync.SyncView;
import de.qabel.desktop.ui.transfer.FxProgressModel;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
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

	@FXML
	private ImageView feedbackButton;

	@FXML
	private ImageView inviteButton;

	@FXML
	private ImageView configButton;

	@FXML
	private ImageView fqaButton;

	@FXML
	private ImageView infoButton;

	@Inject
	private ClientConfiguration clientConfiguration;

	@Inject
	private TransferManager transferManager;

	private HBox browseNav;
	private HBox contactsNav;
	private HBox syncNav;
	private HBox accountingNav;

	@Override
	public void initialize(URL location, ResourceBundle resources) {
		this.resourceBundle = resources;

		navi.getChildren().clear();
		AccountingView accountingView = new AccountingView();
		actionlogView = new ActionlogView();
		accountingNav = createNavItem(resourceBundle.getString("layoutIdentity"), accountingView);
		navi.getChildren().add(accountingNav);

		browseNav = createNavItem(resourceBundle.getString("layoutBrowse"), new RemoteFSView());
		contactsNav = createNavItem(resourceBundle.getString("layoutContacts"), new ContactView());
		syncNav = createNavItem(resourceBundle.getString("layoutSync"), new SyncView());


		navi.getChildren().add(browseNav);
		navi.getChildren().add(contactsNav);
		navi.getChildren().add(syncNav);


		scrollContent.setFillWidth(true);

		if (clientConfiguration.getSelectedIdentity() == null) {
			accountingView.getView(scrollContent.getChildren()::setAll);
			setActiveNavItem(accountingNav);
		}

		updateIdentity();
		clientConfiguration.addObserver((o, arg) -> Platform.runLater(this::updateIdentity));

		uploadProgress.setProgress(0);
		uploadProgress.setDisable(true);

		WindowedTransactionGroup progress = new WindowedTransactionGroup();
		if (transferManager instanceof MonitoredTransferManager) {
			MonitoredTransferManager tm = (MonitoredTransferManager) transferManager;
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

		createButtonGraphics();
	}

	private void createButtonGraphics() {
		Image heartGraphic = new Image(getClass().getResourceAsStream("/img/heart.png"));



		inviteButton.setImage(heartGraphic);
		inviteButton.getStyleClass().add("inline-button");
		inviteButton.setOnMouseClicked(e -> {
			scrollContent.getChildren().setAll(new InviteView().getView());
			activeNavItem.getStyleClass().remove("active");
		});
		Tooltip inviteTooltip = new Tooltip(resourceBundle.getString("layoutIconInviteTooltip"));
		Tooltip.install(inviteButton, inviteTooltip);

		Image exclamationGraphic = new Image(getClass().getResourceAsStream("/img/exclamation.png"));
		feedbackButton.setImage(exclamationGraphic);
		feedbackButton.getStyleClass().add("inline-button");
		feedbackButton.setOnMouseClicked(e -> {
			scrollContent.getChildren().setAll(new FeedbackView().getView());
			activeNavItem.getStyleClass().remove("active");
		});
		Tooltip feebackTooltip = new Tooltip(resourceBundle.getString("layoutIconFeebackTooltip"));
		Tooltip.install(feedbackButton, feebackTooltip);

		Image gearGraphic = new Image(getClass().getResourceAsStream("/img/gear.png"));
		configButton.setImage(gearGraphic);
		configButton.getStyleClass().add("inline-button");

		Image fqaGraphic = new Image(getClass().getResourceAsStream("/img/fqa.png"));
		fqaButton.setImage(fqaGraphic);
		fqaButton.getStyleClass().add("inline-button");

		Image infoGraphic = new Image(getClass().getResourceAsStream("/img/info.png"));
		infoButton.setImage(infoGraphic);
		infoButton.getStyleClass().add("inline-button");
	}


	private String lastAlias;
	private Identity lastIdentity;

	private void updateIdentity() {
		Identity identity = clientConfiguration.getSelectedIdentity();

		browseNav.setManaged(identity != null);
		contactsNav.setManaged(identity != null);
		syncNav.setManaged(identity != null);
		selectedIdentity.setVisible(identity != null);

		avatarContainer.setVisible(identity != null);
		if (identity == null) {
			return;
		}

		final String currentAlias = identity.getAlias();
		if (currentAlias.equals(lastAlias)) {
			return;
		}

		new AvatarView(e -> currentAlias).getViewAsync(avatarContainer.getChildren()::setAll);
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
				scrollContent.getChildren().setAll(view.getView());
				setActiveNavItem(naviItem);
			} catch (Exception exception) {
				exception.printStackTrace();
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
