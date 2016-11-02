package de.qabel.desktop;

import de.qabel.core.config.Account;
import de.qabel.core.config.Identity;
import de.qabel.desktop.config.AccountSelectedEvent;
import de.qabel.desktop.config.ClientConfig;
import de.qabel.desktop.daemon.share.ShareNotificationHandler;
import de.qabel.desktop.event.ClientStartedEvent;
import de.qabel.desktop.event.MainStageShownEvent;
import de.qabel.desktop.inject.DesktopServices;
import de.qabel.desktop.inject.config.StaticRuntimeConfiguration;
import de.qabel.desktop.repository.DropMessageRepository;
import de.qabel.desktop.repository.ShareNotificationRepository;
import de.qabel.desktop.ui.CrashReportAlert;
import de.qabel.desktop.ui.LayoutController;
import de.qabel.desktop.ui.LayoutView;
import de.qabel.desktop.ui.accounting.login.LoginView;
import de.qabel.desktop.ui.actionlog.item.renderer.ShareNotificationRenderer;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.SceneAntialiasing;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.ResourceBundle;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.function.Consumer;

import static de.qabel.desktop.daemon.management.BoxSyncBasedUpload.logger;

public class DesktopClientGui extends Application {
    private Stage primaryStage;
    private LayoutView view;
    ClientConfig config;
    private DesktopServices services;
    private ExecutorService executorService = Executors.newCachedThreadPool();
    private StaticRuntimeConfiguration runtimeConfiguration;

    DesktopClientGui(DesktopServices services, StaticRuntimeConfiguration runtimeConfiguration) {
        this.services = services;
        this.runtimeConfiguration = runtimeConfiguration;
    }

    @Override
    public void start(Stage stage) throws Exception {
        primaryStage = stage;
        runtimeConfiguration.setPrimaryStage(primaryStage);
        config = services.getClientConfiguration();
        setUpWindow();

        Consumer<Account> accountConsumer = account -> {
            initBackgroundServices();
            Platform.runLater(this::showLayoutStage);
        };
        config.onSetAccount(accountConsumer);
        if (!config.hasAccount() || config.getAccount().getToken() == null) {
            showLoginStage();
        } else {
            services.getEventDispatcher().push(new AccountSelectedEvent(config.getAccount()));
            accountConsumer.accept(config.getAccount());
        }

        config.onSelectIdentity(this::addShareMessageRenderer);
        services.getDropMessageRepository().addObserver(new ShareNotificationHandler(getShareRepository()));

        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                Platform.exit();
            }
        });

        services.getEventDispatcher().push(new ClientStartedEvent(primaryStage));
    }

    private void setUpWindow() {
        primaryStage.getIcons().setAll(new Image(getClass().getResourceAsStream("/logo-invert_small.png")));
        Platform.setImplicitExit(false);
        primaryStage.setTitle(getResources().getString("title"));
    }

    public Stage getPrimaryStage() {
        return primaryStage;
    }

    @NotNull
    private Future<?> initBackgroundServices() {
        return executorService.submit(() -> {
            try {
                startTransferManager();
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
        });
    }

    private void showLayoutStage() {
        view = new LayoutView();
        Parent view = this.view.getView();
        runtimeConfiguration.setWindow(((LayoutController) this.view.getPresenter()).getWindow());
        Scene layoutScene = new Scene(view, 900, 600, true, SceneAntialiasing.BALANCED);
        Platform.runLater(() -> primaryStage.setScene(layoutScene));
        primaryStage.show();
        services.getEventDispatcher().push(new MainStageShownEvent(primaryStage));
    }

    private void showLoginStage() {
        Scene loginScene = new Scene(new LoginView().getView(), 370, 570, true, SceneAntialiasing.BALANCED);
        primaryStage.setScene(loginScene);
        primaryStage.show();
    }

    public ResourceBundle getResources() {
        return services.getResourceBundle();
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
