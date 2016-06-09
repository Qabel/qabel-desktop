package de.qabel.desktop;

import com.airhacks.afterburner.injection.Injector;
import com.airhacks.afterburner.views.QabelFXMLView;
import de.qabel.core.config.Contact;
import de.qabel.core.config.Identity;
import de.qabel.core.config.SQLitePersistence;
import de.qabel.core.drop.DropMessage;
import de.qabel.desktop.config.ClientConfig;
import de.qabel.desktop.config.LaunchConfig;
import de.qabel.desktop.daemon.drop.DropDaemon;
import de.qabel.desktop.daemon.share.ShareNotificationHandler;
import de.qabel.desktop.inject.DesktopServices;
import de.qabel.desktop.inject.NewConfigDesktopServiceFactory;
import de.qabel.desktop.inject.RuntimeDesktopServiceFactory;
import de.qabel.desktop.inject.config.StaticRuntimeConfiguration;
import de.qabel.desktop.repository.*;
import de.qabel.desktop.repository.sqlite.*;
import de.qabel.desktop.storage.AbstractNavigation;
import de.qabel.desktop.ui.CrashReportAlert;
import de.qabel.desktop.ui.LayoutController;
import de.qabel.desktop.ui.LayoutView;
import de.qabel.desktop.ui.accounting.login.LoginView;
import de.qabel.desktop.ui.actionlog.PersistenceDropMessage;
import de.qabel.desktop.ui.actionlog.item.renderer.ShareNotificationRenderer;
import de.qabel.desktop.ui.inject.RecursiveInjectionInstanceSupplier;
import de.qabel.desktop.ui.tray.AwtToast;
import de.qabel.desktop.ui.tray.QabelTray;
import de.qabel.desktop.update.HttpUpdateChecker;
import de.qabel.desktop.util.Translator;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.SceneAntialiasing;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.Security;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;
import java.util.ResourceBundle;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import static javafx.scene.control.Alert.AlertType.INFORMATION;
import static javafx.scene.control.Alert.AlertType.WARNING;


public class DesktopClient extends Application {
    private static final String AWS_RECOMMENDED_DNS_CACHE_TTL = "60";
    private static final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
    private static final Logger logger = LoggerFactory.getLogger(DesktopClient.class);
    private static Path LEGACY_DATABASE_FILE = Paths.get(System.getProperty("user.home")).resolve(".qabel/db.sqlite");
    private static Path DATABASE_FILE = Paths.get(System.getProperty("user.home")).resolve(".qabel/config.sqlite");
    private static DesktopServices services;
    private static StaticRuntimeConfiguration runtimeConfiguration;
    private LayoutView view;
    private Stage primaryStage;
    private ExecutorService executorService = Executors.newCachedThreadPool();
    private ClientConfig config;
    private ResourceBundle resources;
    private static Connection connection;

    public static void main(String[] args) throws Exception {
        AbstractNavigation.DEFAULT_AUTOCOMMIT_DELAY = 2000;

        Security.setProperty("networkaddress.cache.ttl",  AWS_RECOMMENDED_DNS_CACHE_TTL);
        String defaultEncoding = System.getProperty("file.encoding");
        if (!defaultEncoding.equals("UTF-8")) {
            logger.warn("default encoding " + defaultEncoding + " is not UTF-8");
        }
        if (args.length > 0) {
            if (args[0].equals("--version")) {
                System.out.println(appVersion());
                System.exit(-1);
            }
            DATABASE_FILE = new File(args[0]).getAbsoluteFile().toPath();
        }

        LaunchConfig launchConfig = new LaunchConfigurationReader(
            DesktopClient.class.getResourceAsStream("/launch.properties")
        ).load();

        runtimeConfiguration = new StaticRuntimeConfiguration(
            launchConfig.getDropUrl().toString(),
            LEGACY_DATABASE_FILE,
            getConfigDatabase()
        );
        RuntimeDesktopServiceFactory staticDesktopServiceFactory = new NewConfigDesktopServiceFactory(
            runtimeConfiguration,
            new SqliteTransactionManager(connection)
        );
        services = staticDesktopServiceFactory;
        Injector.setConfigurationSource(key -> staticDesktopServiceFactory.get((String) key));
        Injector.setInstanceSupplier(new RecursiveInjectionInstanceSupplier(staticDesktopServiceFactory));

        System.setProperty("prism.lcdtext", "false");
        UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
        launch(args);
    }

    private static ClientDatabase getConfigDatabase() {
        boolean needsToMigrateLegacyDatabase = !Files.exists(DATABASE_FILE) && Files.exists(LEGACY_DATABASE_FILE);

        try {
            try {
                Path configDir = DATABASE_FILE.toAbsolutePath().getParent();
                if (!Files.isDirectory(configDir)) {
                    Files.createDirectory(configDir);
                }
                connection = DriverManager.getConnection("jdbc:sqlite://" + DATABASE_FILE.toAbsolutePath());

                try {
                    try (Statement statement = connection.createStatement()) {
                        statement.execute("PRAGMA FOREIGN_KEYS = ON");
                    }
                    ClientDatabase clientDatabase = new DesktopClientDatabase(connection);

                    clientDatabase.migrate();

                    // TODO cut below here after the config migration transition period (~ 03.05.2016)
                    if (needsToMigrateLegacyDatabase) {
                        logger.warn("found legacy database, migrating to new format");
                        TransactionManager tm = new SqliteTransactionManager(connection);
                        LegacyDatabaseMigrator legacyDatabaseMigrator = new LegacyDatabaseMigrator(
                            new SQLitePersistence(LEGACY_DATABASE_FILE.toAbsolutePath().toString()),
                            clientDatabase
                        );
                        tm.transactional(legacyDatabaseMigrator::migrate);
                    }

                    return clientDatabase;
                } catch (Exception e) {
                    try { connection.close(); } catch (Exception ignored) {}
                    throw e;
                }

            } catch (Exception e) {
                throw new IllegalStateException("failed to initialize or migrate config database:" + e.getMessage(), e);
            }
        } catch (Exception e) {
            if (needsToMigrateLegacyDatabase) {
                try {
                    Files.delete(DATABASE_FILE);
                } catch (Exception ignored) {
                }
            }
            throw e;
        }
    }

    public static String appVersion() throws IOException {
        return IOUtils.toString(DesktopClient.class.getResourceAsStream("/version"));
    }

    private void checkVersion() {
        try {
            HttpUpdateChecker checker = new HttpUpdateChecker();
            String currentVersion = appVersion();

            if (currentVersion.equals("dev")) {
                return;
            }

            if (!checker.isCurrent(currentVersion)) {
                final boolean required = !checker.isAllowed(currentVersion);

                ButtonType cancelButton = new ButtonType(resources.getString("updateCancel"), ButtonBar.ButtonData.CANCEL_CLOSE);
                ButtonType updateButton = new ButtonType(resources.getString("updateStart"), ButtonBar.ButtonData.APPLY);
                String message = required ? resources.getString("updateRequired") : resources.getString("updatePossible");
                Alert alert = new Alert(required ? WARNING : INFORMATION, message, cancelButton, updateButton);
                alert.setHeaderText(null);
                alert.showAndWait().ifPresent(buttonType -> {
                    if (buttonType == updateButton) {
                        getHostServices().showDocument(checker.getDesktopVersion().getDownloadURL());
                        System.exit(-1);
                    }
                    if (required) {
                        System.exit(-1);
                    }
                });
            }
        } catch (Exception e) {
            logger.error("failed to check for updates: " + e.getMessage(), e);
        }
    }

    @Override
    public void start(Stage stage) throws Exception {
        primaryStage = stage;
        setUserAgentStylesheet(STYLESHEET_MODENA);

        checkVersion();
        runtimeConfiguration.setPrimaryStage(primaryStage);
        config = services.getClientConfiguration();

        SceneAntialiasing aa = SceneAntialiasing.BALANCED;
        primaryStage.getIcons().setAll(new Image(getClass().getResourceAsStream("/logo-invert_small.png")));
        Scene scene;
        resources = QabelFXMLView.getDefaultResourceBundle();

        Platform.setImplicitExit(false);
        primaryStage.setTitle(resources.getString("title"));
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

        runtimeConfiguration.loadAboutFiles();

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
                        .renderString(dropMessage.getDropPayload(), services.getResourceBundle());
                    Translator translator = services.getTranslator();
                    String title = translator.getString("newMessageNotification", sender.getAlias());
                    Platform.runLater(() -> tray.showNotification(title, content));
                },
                1,
                TimeUnit.SECONDS
            )
        );
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
