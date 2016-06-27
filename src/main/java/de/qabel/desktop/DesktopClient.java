package de.qabel.desktop;

import de.qabel.box.storage.AbstractNavigation;
import de.qabel.core.config.SQLitePersistence;
import de.qabel.desktop.config.LaunchConfig;
import de.qabel.desktop.inject.DesktopServices;
import de.qabel.desktop.inject.NewConfigDesktopServiceFactory;
import de.qabel.desktop.inject.RuntimeDesktopServiceFactory;
import de.qabel.desktop.inject.config.StaticRuntimeConfiguration;
import de.qabel.desktop.repository.TransactionManager;
import de.qabel.desktop.repository.sqlite.ClientDatabase;
import de.qabel.desktop.repository.sqlite.DesktopClientDatabase;
import de.qabel.desktop.repository.sqlite.LegacyDatabaseMigrator;
import de.qabel.desktop.repository.sqlite.SqliteTransactionManager;
import de.qabel.desktop.ui.inject.AfterburnerInjector;
import de.qabel.desktop.ui.inject.RecursiveInjectionInstanceSupplier;
import de.qabel.desktop.ui.util.FXApplicationLauncher;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.Security;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;


public class DesktopClient {
    private static final String AWS_RECOMMENDED_DNS_CACHE_TTL = "60";
    private static Logger logger;
    private static Path LEGACY_DATABASE_FILE = Paths.get(System.getProperty("user.home")).resolve(".qabel/db.sqlite");
    private static Path DATABASE_FILE = Paths.get(System.getProperty("user.home")).resolve(".qabel/config.sqlite");
    private static StaticRuntimeConfiguration runtimeConfiguration;
    private static Connection connection;
    private static DesktopServices services;

    public static void main(String[] args) throws Exception {
        System.setProperty("log.root", DATABASE_FILE.getParent().toAbsolutePath().toString());
        logger = LoggerFactory.getLogger(DesktopClient.class);
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

        Path launchConfigOverwrite = Paths.get("launch.properties");
        LaunchConfig launchConfig;
        if (Files.exists(launchConfigOverwrite)) {
            launchConfig = new LaunchConfigurationReader(new FileInputStream(launchConfigOverwrite.toFile())).load();
        } else {
             launchConfig = new LaunchConfigurationReader(
                DesktopClient.class.getResourceAsStream("/launch.properties")
            ).load();
        }

        runtimeConfiguration = new StaticRuntimeConfiguration(
            launchConfig,
            LEGACY_DATABASE_FILE,
            getConfigDatabase()
        );
        RuntimeDesktopServiceFactory staticDesktopServiceFactory = new NewConfigDesktopServiceFactory(
            runtimeConfiguration,
            new SqliteTransactionManager(connection)
        );
        services = staticDesktopServiceFactory;
        AfterburnerInjector.setConfigurationSource(key -> staticDesktopServiceFactory.get((String) key));
        AfterburnerInjector.setInstanceSupplier(new RecursiveInjectionInstanceSupplier(staticDesktopServiceFactory));

        System.setProperty("prism.lcdtext", "false");
        UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
        DesktopClientGui app = new DesktopClientGui(services, runtimeConfiguration);
        new FXApplicationLauncher().launch(app);
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
}
