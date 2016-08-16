package de.qabel.desktop.ui;

import com.airhacks.afterburner.views.FXMLView;
import de.qabel.core.config.Identity;
import de.qabel.desktop.config.ClientConfig;
import de.qabel.desktop.daemon.management.MonitoredTransferManager;
import de.qabel.desktop.daemon.management.TransferManager;
import de.qabel.desktop.daemon.management.WindowedTransactionGroup;
import de.qabel.desktop.repository.DropMessageRepository;
import de.qabel.desktop.ui.about.AboutView;
import de.qabel.desktop.ui.accounting.AccountingView;
import de.qabel.desktop.ui.accounting.avatar.AvatarView;
import de.qabel.desktop.ui.actionlog.Actionlog;
import de.qabel.desktop.ui.actionlog.ActionlogView;
import de.qabel.desktop.ui.actionlog.FxActionlog;
import de.qabel.desktop.ui.contact.ContactView;
import de.qabel.desktop.ui.feedback.FeedbackView;
import de.qabel.desktop.ui.invite.InviteView;
import de.qabel.desktop.ui.remotefs.RemoteFSView;
import de.qabel.desktop.ui.sync.SyncView;
import de.qabel.desktop.ui.transfer.ComposedProgressBar;
import de.qabel.desktop.ui.transfer.TransferViewModel;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Tooltip;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;

import javax.inject.Inject;
import java.awt.*;
import java.net.URI;
import java.net.URL;
import java.util.ResourceBundle;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class LayoutController extends AbstractController implements Initializable {
    private ExecutorService executor = Executors.newCachedThreadPool();
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
    private NaviItem activeNavItem;

    @FXML
    private Pane avatarContainer;

    @FXML
    private Pane selectedIdentity;

    @FXML
    private ScrollPane scroll;

    @FXML
    private ProgressBar uploadProgress;

    @FXML
    ImageView feedbackButton;

    @FXML
    ImageView inviteButton;

    @FXML
    ImageView configButton;

    @FXML
    ImageView faqButton;

    @FXML
    ImageView infoButton;

    @FXML
    private Pane window;

    @FXML
    private VBox bottomContainer;

    @Inject
    private ClientConfig clientConfiguration;

    @Inject
    private TransferManager transferManager;

    @Inject
    private DropMessageRepository dropMessageRepository;

    @FXML
    Label quota;
    @FXML
    Label quotaDescription;
    @FXML
    Label provider;
    @FXML
    Label faqBackground;
    @FXML
    Label inviteBackground;
    @FXML
    Label feedbackBackground;

    NaviItem browseNav;
    NaviItem contactsNav;
    NaviItem syncNav;
    NaviItem accountingNav;
    NaviItem aboutNav;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        resourceBundle = resources;

        navi.getChildren().clear();
        AccountingView accountingView = new AccountingView();
        actionlogView = new ActionlogView();
        accountingNav = createNavItem(resourceBundle.getString("layoutIdentity"),
            new Image(getClass().getResourceAsStream("/img/account_white.png")),
            accountingView);
        navi.getChildren().add(accountingNav);
        browseNav = createNavItem(resourceBundle.getString("layoutBrowse"),
            new Image(getClass().getResourceAsStream("/img/folder_white.png")),
            new RemoteFSView());
        contactsNav = createNavItem(resourceBundle.getString("layoutContacts"),
            new Image(getClass().getResourceAsStream("/img/account_multiple_white.png")),
            new ContactView());
        syncNav = createNavItem(resourceBundle.getString("layoutSync"),
            new Image(getClass().getResourceAsStream("/img/sync_white.png")),
            new SyncView());
        aboutNav = createNavItem(resourceBundle.getString("layoutAbout"),
            new Image(getClass().getResourceAsStream("/img/information_white.png")),
            new AboutView());

        navi.getChildren().add(browseNav);
        navi.getChildren().add(contactsNav);
        navi.getChildren().add(syncNav);
        navi.getChildren().add(aboutNav);

        scrollContent.setFillWidth(true);

        if (clientConfiguration.getSelectedIdentity() == null) {
            accountingView.getView(scrollContent.getChildren()::setAll);
            setActiveNavItem(accountingNav);
        }

        updateIdentity();
        clientConfiguration.onSelectIdentity(i -> Platform.runLater(this::updateIdentity));

        bottomContainer.getChildren().remove(uploadProgress);
        ComposedProgressBar progressBar = new ComposedProgressBar();
        progressBar.getStylesheets().addAll(window.getStylesheets());
        bottomContainer.getChildren().add(0, progressBar);

        WindowedTransactionGroup progress = new WindowedTransactionGroup();
        if (transferManager instanceof MonitoredTransferManager) {
            MonitoredTransferManager tm = (MonitoredTransferManager) transferManager;
            tm.onAdd(progress::add);
        }
        TransferViewModel progressModel = new TransferViewModel(progress);
        progressBar.getTotalProgress().progressProperty().bind(progressModel.progressProperty());
        progressBar.visibleProperty().bind(progressModel.runningProperty());
        progressBar.getItemStatusLabel().textProperty().bind(progressModel.currentTransactionPercentLabel());
        progressBar.getSyncStatusLabel().visibleProperty().bind(progressModel.currentItemsProperty().greaterThanOrEqualTo(0));
        progressBar.getSyncStatusLabel().textProperty().bind(
            progressModel.currentItemsProperty().asString()
                .concat(" / ")
                .concat(progressModel.totalItemsProperty())
        );

        createButtonGraphics();

        new OfflineView().getViewAsync(window.getChildren()::add);

        FxActionlog log = new FxActionlog(new Actionlog(dropMessageRepository));
        Indicator newMessageIndicator = contactsNav.getIndicator();
        newMessageIndicator.textProperty().bind(log.unseenMessageCountProperty().asString());
        newMessageIndicator.visibleProperty().bind(newMessageIndicator.textProperty().isNotEqualTo("0"));
    }

    private void createButtonGraphics() {
        Image heartGraphic = new Image(getClass().getResourceAsStream("/img/heart_white.png"));
        inviteButton.setImage(heartGraphic);
        inviteButton.getStyleClass().add("inline-button");
        inviteButton.setOnMouseClicked(e -> {
            scrollContent.getChildren().setAll(new InviteView().getView());
            setActivityMenu(inviteBackground, inviteButton);
        });
        Tooltip inviteTooltip = new Tooltip(resourceBundle.getString("layoutIconInviteTooltip"));
        Tooltip.install(inviteButton, inviteTooltip);

        Image exclamationGraphic = new Image(getClass().getResourceAsStream("/img/exclamation_white.png"));
        feedbackButton.setImage(exclamationGraphic);
        feedbackButton.getStyleClass().add("inline-button");
        feedbackButton.setOnMouseClicked(e -> {
            scrollContent.getChildren().setAll(new FeedbackView().getView());
            setActivityMenu(feedbackBackground, feedbackButton);
        });
        Tooltip feebackTooltip = new Tooltip(resourceBundle.getString("layoutIconFeebackTooltip"));
        Tooltip.install(feedbackButton, feebackTooltip);

        /*
        Image gearGraphic = new Image(getClass().getResourceAsStream("/img/gear.png"));
        configButton.setImage(gearGraphic);
        configButton.getStyleClass().add("inline-button");
        */

        Image faqGraphics = new Image(getClass().getResourceAsStream("/img/faq_white.png"));
        faqButton.setImage(faqGraphics);
        faqButton.getStyleClass().add("inline-button");
        faqButton.setOnMouseClicked(e -> {
            setActivityMenu(faqBackground, faqButton);
            executor.submit(() -> {
                try {
                    Desktop.getDesktop().browse(new URI(resourceBundle.getString("faqUrl")));
                } catch (Exception e1) {
                    alert("failed to open FAQ: " + e1.getMessage(), e1);
                }
            });
        });
        Tooltip faq = new Tooltip(resourceBundle.getString("layoutIconFaqTooltip"));
        Tooltip.install(faqButton, faq);

        /*
        Image infoGraphic = new Image(getClass().getResourceAsStream("/img/info.png"));
        infoButton.setImage(infoGraphic);
        infoButton.getStyleClass().add("inline-button");
        */
    }


    private void setActivityMenu(Label label, ImageView icon) {
        cleanIconMenuStyle();
        label.setVisible(true);
        icon.getStyleClass().add("darkgrey");

        if (activeNavItem != null) {
            activeNavItem.setActive(false);
        }
    }

    private void cleanIconMenuStyle() {
        inviteBackground.setVisible(false);
        faqBackground.setVisible(false);
        feedbackBackground.setVisible(false);
//        inviteButton.getStyleClass().clear();
//        faqButton.getStyleClass().clear();
//        feedbackButton.getStyleClass().clear();
        inviteButton.getStyleClass().remove("darkgrey");
        faqButton.getStyleClass().remove("darkgrey");
        feedbackButton.getStyleClass().remove("darkgrey");
    }

    private String lastAlias;
    private Identity lastIdentity;

    private void updateIdentity() {
        Identity identity = clientConfiguration.getSelectedIdentity();

        browseNav.setManaged(identity != null);
        contactsNav.setManaged(identity != null);
        syncNav.setManaged(identity != null);

        browseNav.setVisible(identity != null);
        contactsNav.setVisible(identity != null);
        syncNav.setVisible(identity != null);

        selectedIdentity.setVisible(identity != null);

        avatarContainer.setVisible(identity != null);
        if (identity == null) {
            return;
        }

        final String currentAlias = identity.getAlias();
        if (currentAlias.equals(lastAlias)) {
            return;
        }

        identity.attach(() -> Platform.runLater(() -> {
            alias.setText(identity.getAlias());
            updateAvatar(identity);
        }));

        new AvatarView(e -> currentAlias).getViewAsync(avatarContainer.getChildren()::setAll);
        alias.setText(currentAlias);
        lastAlias = currentAlias;

        if (clientConfiguration.getAccount() == null) {
            return;
        }
        mail.setText(clientConfiguration.getAccount().getUser());
    }

    private void updateAvatar(Identity identity) {
        new AvatarView(e -> identity.getAlias()).getViewAsync(avatarContainer.getChildren()::setAll);
    }

    private NaviItem createNavItem(String label, Image image, FXMLView view) {
        NaviItem naviItem = new NaviItem(label, image, view);
        naviItem.setOnAction(e -> {
            try {
                scrollContent.getChildren().setAll(view.getView());
                setActiveNavItem(naviItem);
                cleanIconMenuStyle();
            } catch (Exception exception) {
                exception.printStackTrace();
                alert(exception.getMessage(), exception);
            }
        });
        return naviItem;
    }

    private void setActiveNavItem(NaviItem naviItem) {
        if (activeNavItem != null) {
            activeNavItem.setActive(false);
        }
        naviItem.setActive(true);
        activeNavItem = naviItem;
    }

    public Object getScroller() {
        return scroll;
    }

    public Pane getWindow() {
        return window;
    }
}
