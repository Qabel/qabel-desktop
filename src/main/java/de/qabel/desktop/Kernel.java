package de.qabel.desktop;

import com.sun.javafx.application.HostServicesDelegate;
import com.sun.javafx.application.PlatformImpl;
import de.qabel.box.storage.AbstractNavigation;
import de.qabel.desktop.config.LaunchConfig;
import de.qabel.desktop.inject.DesktopServices;
import de.qabel.desktop.inject.NewConfigDesktopServiceFactory;
import de.qabel.desktop.inject.RuntimeDesktopServiceFactory;
import de.qabel.desktop.inject.config.StaticRuntimeConfiguration;
import de.qabel.desktop.repository.sqlite.ClientDatabase;
import de.qabel.desktop.repository.sqlite.DesktopClientDatabase;
import de.qabel.desktop.repository.sqlite.SqliteTransactionManager;
import de.qabel.desktop.ui.inject.AfterburnerInjector;
import de.qabel.desktop.ui.inject.RecursiveInjectionInstanceSupplier;
import de.qabel.desktop.ui.util.FXApplicationLauncher;
import de.qabel.desktop.ui.util.PlatformUtils;
import de.qabel.desktop.update.HttpUpdateChecker;
import de.qabel.desktop.update.UpdateChecker;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.Security;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;
import java.util.ResourceBundle;
import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

import static javafx.scene.control.Alert.AlertType.INFORMATION;
import static javafx.scene.control.Alert.AlertType.WARNING;

public class Kernel {
    private static final String AWS_RECOMMENDED_DNS_CACHE_TTL = "60";
    private static final Logger logger = LoggerFactory.getLogger(Kernel.class);

    private Path databaseFile = Paths.get(System.getProperty("user.home")).resolve(".qabel/config.sqlite");
    private StaticRuntimeConfiguration runtimeConfiguration;
    private Connection connection;
    private DesktopServices services;
    DesktopClientGui app;

    private final String currentVersion;

    private Callable<LaunchConfig> launchConfigLoader = this::loadLaunchConfig;
    private Callable<ClientDatabase> configDatabaseLoader = this::getConfigDatabase;
    private Runnable shutdown = this::shutdown;
    private UpdateChecker checker = new HttpUpdateChecker();
    private RuntimeDesktopServiceFactory staticDesktopServiceFactory;
    Consumer<String> documentLauncher;
    private LaunchConfig launchConfig;

    public Kernel(String currentVersion) {
        this.currentVersion = currentVersion;
    }

    public Kernel(String currentVersion, LaunchConfig launchConfig) {
        this(currentVersion);
        this.launchConfig = launchConfig;
    }

    public void initialize() throws Exception {
        System.setProperty("log.root", databaseFile.getParent().toAbsolutePath().toString());
        Security.setProperty("networkaddress.cache.ttl",  AWS_RECOMMENDED_DNS_CACHE_TTL);
        AbstractNavigation.DEFAULT_AUTOCOMMIT_DELAY = 2000;

        String defaultEncoding = System.getProperty("file.encoding");
        if (!defaultEncoding.equals("UTF-8")) {
            logger.warn("default encoding " + defaultEncoding + " is not UTF-8");
        }

        initContainer();
        initGui();
        documentLauncher = HostServicesDelegate.getInstance(app)::showDocument;
    }

    public void initGui() throws ClassNotFoundException, InstantiationException, IllegalAccessException, UnsupportedLookAndFeelException {
        AfterburnerInjector.setConfigurationSource(key -> staticDesktopServiceFactory.get((String) key));
        AfterburnerInjector.setInstanceSupplier(new RecursiveInjectionInstanceSupplier(staticDesktopServiceFactory));
        System.setProperty("prism.lcdtext", "false");
        UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
        app = new DesktopClientGui(services, runtimeConfiguration);

        cancelButton = new ButtonType(getResources().getString("updateCancel"), ButtonBar.ButtonData.CANCEL_CLOSE);
        updateButton = new ButtonType(getResources().getString("updateStart"), ButtonBar.ButtonData.APPLY);
    }

    public void initContainer() throws Exception {
        if (launchConfig == null) {
            launchConfig = launchConfigLoader.call();
        }
        runtimeConfiguration = new StaticRuntimeConfiguration(launchConfig, configDatabaseLoader.call());
        runtimeConfiguration.setCurrentVersion(currentVersion);
        staticDesktopServiceFactory = new NewConfigDesktopServiceFactory(
            runtimeConfiguration,
            new SqliteTransactionManager(connection)
        );
        services = staticDesktopServiceFactory;
    }

    public DesktopServices getContainer() {
        return services;
    }

    private LaunchConfig loadLaunchConfig() throws FileNotFoundException {
        Path launchConfigOverwrite = Paths.get("launch.properties");
        if (Files.exists(launchConfigOverwrite)) {
            return new LaunchConfigurationReader(new FileInputStream(launchConfigOverwrite.toFile())).load();
        } else {
            return new LaunchConfigurationReader(
                DesktopClient.class.getResourceAsStream("/launch.properties")
            ).load();
        }
    }

    public void setDatabaseFile(Path databaseFile) {
        this.databaseFile = databaseFile;
    }

    public void start() throws Exception {
        PlatformUtils.start();
        checkVersion();
        new FXApplicationLauncher().launch(app);
    }

    public ResourceBundle getResources() {
        return getContainer().getResourceBundle();
    }

    ButtonType cancelButton;
    ButtonType updateButton;
    AtomicReference<Alert> alertRef = new AtomicReference<>();
    private void checkVersion() {
        try {
            if (currentVersion.equals("dev")) {
                return;
            }

            if (!checker.isCurrent(currentVersion)) {
                final boolean required = !checker.isAllowed(currentVersion);

                String message = required ? getResources().getString("updateRequired") : getResources().getString("updatePossible");
                PlatformImpl.runAndWait(() -> {
                    Alert alert = new Alert(required ? WARNING : INFORMATION, message, cancelButton, updateButton);
                    alertRef.set(alert);
                    alert.setHeaderText(null);
                    alert.showAndWait().ifPresent(buttonType -> {
                        if (buttonType == updateButton) {
                            try {
                                documentLauncher.accept(checker.getDesktopVersion().getDownloadURL());
                                shutdown.run();
                            } catch (Exception e) {
                                logger.error("failed to open download: " + e.getMessage(), e);
                            }
                        }
                        if (required) {
                            shutdown.run();
                        }
                    });
                });
            }
        } catch (Exception e) {
            logger.error("failed to check for updates: " + e.getMessage(), e);
        }
    }

    private void shutdown() {
        PlatformUtils.shutdown();
        System.exit(-1);
    }

    private ClientDatabase getConfigDatabase() {
        try {
            try {
                Path configDir = databaseFile.toAbsolutePath().getParent();
                if (!Files.isDirectory(configDir)) {
                    Files.createDirectory(configDir);
                }
                connection = DriverManager.getConnection(getSqliteConnectionString());

                try {
                    try (Statement statement = connection.createStatement()) {
                        statement.execute("PRAGMA FOREIGN_KEYS = ON");
                    }
                    ClientDatabase clientDatabase = new DesktopClientDatabase(connection);
                    clientDatabase.migrate();

                    return clientDatabase;
                } catch (Exception e) {
                    try { connection.close(); } catch (Exception ignored) {}
                    throw e;
                }

            } catch (Exception e) {
                throw new IllegalStateException("failed to initialize or migrate config database:" + e.getMessage(), e);
            }
        } catch (Exception e) {
            throw e;
        }
    }

    protected String getSqliteConnectionString() {
        return "jdbc:sqlite://" + databaseFile.toAbsolutePath();
    }

    public void setConfigDatabaseLoader(Callable<ClientDatabase> configDatabaseLoader) {
        this.configDatabaseLoader = configDatabaseLoader;
    }

    public void setLaunchConfigLoader(Callable<LaunchConfig> launchConfigLoader) {
        this.launchConfigLoader = launchConfigLoader;
    }

    public void setShutdown(Runnable shutdown) {
        this.shutdown = shutdown;
    }

    public void setChecker(UpdateChecker checker) {
        this.checker = checker;
    }
}
