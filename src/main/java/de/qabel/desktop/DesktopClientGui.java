package de.qabel.desktop;

import de.qabel.core.config.Contact;
import de.qabel.core.config.Identity;
import de.qabel.core.drop.DropMessage;
import de.qabel.desktop.config.ClientConfig;
import de.qabel.desktop.daemon.share.ShareNotificationHandler;
import de.qabel.desktop.inject.DesktopServices;
import de.qabel.desktop.inject.config.StaticRuntimeConfiguration;
import de.qabel.desktop.repository.DropMessageRepository;
import de.qabel.desktop.repository.ShareNotificationRepository;
import de.qabel.desktop.ui.CrashReportAlert;
import de.qabel.desktop.ui.LayoutController;
import de.qabel.desktop.ui.LayoutView;
import de.qabel.desktop.ui.accounting.login.LoginView;
import de.qabel.desktop.ui.actionlog.PersistenceDropMessage;
import de.qabel.desktop.ui.actionlog.item.renderer.ShareNotificationRenderer;
import de.qabel.desktop.ui.tray.AwtToast;
import de.qabel.desktop.ui.tray.QabelTray;
import de.qabel.desktop.util.Translator;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.SceneAntialiasing;
import javafx.scene.image.Image;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.ResourceBundle;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import static de.qabel.desktop.daemon.management.BoxSyncBasedUpload.logger;

public class DesktopClientGui extends Application {
    private Stage primaryStage;
    private LayoutView view;
    private ClientConfig config;
    private DesktopServices services;
    private ExecutorService executorService = Executors.newCachedThreadPool();
    private StaticRuntimeConfiguration runtimeConfiguration;
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

    public DesktopClientGui(DesktopServices services, StaticRuntimeConfiguration runtimeConfiguration) {
        this.services = services;
        this.runtimeConfiguration = runtimeConfiguration;
    }

    @Override
    public void start(Stage stage) throws Exception {
        primaryStage = stage;
        runtimeConfiguration.setPrimaryStage(primaryStage);
        config = services.getClientConfiguration();

        SceneAntialiasing aa = SceneAntialiasing.BALANCED;
        primaryStage.getIcons().setAll(new Image(getClass().getResourceAsStream("/logo-invert_small.png")));
        Scene scene;

        Platform.setImplicitExit(false);
        primaryStage.setTitle(getResources().getString("title"));
        scene = new Scene(new LoginView().getView(), 370, 570, true, aa);
        primaryStage.setScene(scene);

        config.onSetAccount(account ->
            executorService.submit(() -> {
                try {
                    startTransferManager();
                    startSyncDaemon();
                    startDropDaemon();
                    view = new LayoutView();
                    Parent view = this.view.getView();
                    runtimeConfiguration.setWindow(((LayoutController)this.view.getPresenter()).getWindow());
                    Scene layoutScene = new Scene(view, 900, 600, true, aa);
                    Platform.runLater(() -> primaryStage.setScene(layoutScene));

                    if (config.getSelectedIdentity() != null) {
                        addShareMessageRenderer(config.getSelectedIdentity());
                    }
                } catch (Exception e) {
                    logger.error("failed to init background services: " + e.getMessage(), e);
                    Platform.runLater(() -> {
                        final CrashReportAlert alert = new CrashReportAlert(
                            services.getCrashReportHandler(),
                            "failed to init brackground services",
                            e
                        );
                        alert.showAndWait();
                        System.exit(-1);
                    });
                }
            })
        );
        config.onSelectIdentity(this::addShareMessageRenderer);

        QabelTray tray = new QabelTray(primaryStage, new AwtToast());
        tray.install();
        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                Platform.exit();
            }
        });
        primaryStage.show();

        services.getDropMessageRepository().addObserver(new ShareNotificationHandler(getShareRepository()));
        services.getDropMessageRepository().addObserver(
            (o, arg) -> scheduler.schedule(
                () -> {
                    if (!(arg instanceof PersistenceDropMessage)) {
                        return;
                    }
                    PersistenceDropMessage message = (PersistenceDropMessage) arg;
                    if (message.isSent() || message.isSeen()) {
                        return;
                    }
                    Contact sender = (Contact) message.getSender();
                    DropMessage dropMessage = message.getDropMessage();
                    String content = services.getDropMessageRendererFactory()
                        .getRenderer(dropMessage.getDropPayloadType())
                        .renderString(dropMessage.getDropPayload(), getResources());
                    Translator translator = services.getTranslator();
                    String title = translator.getString("newMessageNotification", sender.getAlias());
                    Platform.runLater(() -> tray.showNotification(title, content));
                },
                1,
                TimeUnit.SECONDS
            )
        );
    }

    public ResourceBundle getResources() {
        return services.getResourceBundle();
    }

    private void startDropDaemon() {
        new Thread(services.getDropDaemon()).start();
    }

    private void startSyncDaemon() {
        new Thread(services.getSyncDaemon()).start();
    }

    private ShareNotificationRepository getShareRepository() {
        return services.getShareNotificationRepository();
    }

    private void addShareMessageRenderer(Identity arg) {
        executorService.submit(() -> {
            try {
                ShareNotificationRenderer renderer = new ShareNotificationRenderer(
                    services.getBoxVolumeFactory().getVolume(config.getAccount(), arg).getReadBackend(),
                    services.getSharingService()
                );
                services.getDropMessageRendererFactory().addRenderer(DropMessageRepository.PAYLOAD_TYPE_SHARE_NOTIFICATION, renderer);
            } catch (IOException e) {
                logger.warn(e.getMessage(), e);
            }
        });
    }

    private void startTransferManager() {
        new Thread(services.getTransferManager(), "TransactionManager").start();
    }

}
