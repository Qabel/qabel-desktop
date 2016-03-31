package de.qabel.desktop;

import com.airhacks.afterburner.injection.Injector;
import com.airhacks.afterburner.views.QabelFXMLView;
import de.qabel.core.config.Account;
import de.qabel.core.config.Identity;
import de.qabel.desktop.config.ClientConfiguration;
import de.qabel.desktop.daemon.drop.DropDaemon;
import de.qabel.desktop.daemon.share.ShareNotificationHandler;
import de.qabel.desktop.daemon.sync.SyncDaemon;
import de.qabel.desktop.daemon.sync.worker.DefaultSyncerFactory;
import de.qabel.desktop.inject.DesktopServices;
import de.qabel.desktop.inject.StaticDesktopServiceFactory;
import de.qabel.desktop.inject.config.StaticRuntimeConfiguration;
import de.qabel.desktop.repository.DropMessageRepository;
import de.qabel.desktop.repository.exception.EntityNotFoundExcepion;
import de.qabel.desktop.repository.exception.PersistenceException;
import de.qabel.desktop.storage.AbstractNavigation;
import de.qabel.desktop.ui.LayoutView;
import de.qabel.desktop.ui.accounting.login.LoginView;
import de.qabel.desktop.ui.actionlog.item.renderer.ShareNotificationRenderer;
import de.qabel.desktop.ui.inject.RecursiveInjectionInstanceSupplier;
import de.qabel.desktop.update.HttpUpdateChecker;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.SceneAntialiasing;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.stage.Stage;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.ResourceBundle;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static javafx.scene.control.Alert.AlertType.INFORMATION;
import static javafx.scene.control.Alert.AlertType.WARNING;


public class DesktopClient extends Application {
    private static final Logger logger = LoggerFactory.getLogger(DesktopClient.class.getSimpleName());
    private static Path DATABASE_FILE = Paths.get(System.getProperty("user.home")).resolve(".qabel/db.sqlite");
    private static DesktopServices services;
    private static StaticRuntimeConfiguration runtimeConfiguration;
    private boolean inBound;
    private LayoutView view;
    private Stage primaryStage;
    private ExecutorService executorService = Executors.newCachedThreadPool();
    private ClientConfiguration config;
    private boolean visible = false;
    private ResourceBundle resources;

    public static void main(String[] args) throws Exception {
        AbstractNavigation.DEFAULT_AUTOCOMMIT_DELAY = 2000;

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

        runtimeConfiguration = new StaticRuntimeConfiguration("https://drop.qabel.de", DATABASE_FILE);
        StaticDesktopServiceFactory staticDesktopServiceFactory = new StaticDesktopServiceFactory(runtimeConfiguration);
        services = staticDesktopServiceFactory;
        Injector.setConfigurationSource(key -> staticDesktopServiceFactory.get((String) key));
        Injector.setInstanceSupplier(new RecursiveInjectionInstanceSupplier(staticDesktopServiceFactory));

        System.setProperty("prism.lcdtext", "false");
        UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
        launch(args);
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
        primaryStage.getIcons().setAll(new javafx.scene.image.Image(getClass().getResourceAsStream("/logo-invert_small.png")));
        Scene scene;
        resources = QabelFXMLView.getDefaultResourceBundle();

        Platform.setImplicitExit(false);
        primaryStage.setTitle(resources.getString("title"));
        scene = new Scene(new LoginView().getView(), 370, 570, true, aa);
        primaryStage.setScene(scene);

        config.addObserver((o, arg) -> {
            Platform.runLater(() -> {
                if (arg instanceof Account) {
                    try {
                        new Thread(getSyncDaemon(config)).start();
                        new Thread(getDropDaemon(config)).start();
                        view = new LayoutView();
                        Parent view = this.view.getView();
                        Scene layoutScene = new Scene(view, 900, 600, true, aa);
                        Platform.runLater(() -> primaryStage.setScene(layoutScene));

                        if (config.getSelectedIdentity() != null) {
                            addShareMessageRenderer(config.getSelectedIdentity());
                        }
                    } catch (Exception e) {
                        logger.error("failed to init background services: " + e.getMessage(), e);
                        //TODO to something with the fault
                    }
                } else if (arg instanceof Identity) {
                    addShareMessageRenderer((Identity) arg);
                }
            });
        });

        services.getDropMessageRepository().addObserver(new ShareNotificationHandler(config));

        setTrayIcon(primaryStage);

        Runtime.getRuntime().addShutdownHook(new Thread() {
            public void run() {
                Platform.exit();
            }
        });
        primaryStage.show();
    }

    private void addShareMessageRenderer(Identity arg) {
        executorService.submit(() -> {
            ShareNotificationRenderer renderer = new ShareNotificationRenderer(
                services.getBoxVolumeFactory().getVolume(config.getAccount(), arg).getReadBackend(),
                services.getSharingService()
            );
            services.getDropMessageRendererFactory().addRenderer(DropMessageRepository.PAYLOAD_TYPE_SHARE_NOTIFICATION, renderer);
        });
    }

    protected SyncDaemon getSyncDaemon(ClientConfiguration config) throws IOException {
        new Thread(services.getTransferManager(), "TransactionManager").start();
        return new SyncDaemon(config.getBoxSyncConfigs(), new DefaultSyncerFactory(services.getBoxVolumeFactory(), services.getTransferManager()));
    }

    protected DropDaemon getDropDaemon(ClientConfiguration config) throws PersistenceException, EntityNotFoundExcepion {
        return new DropDaemon(config, services.getDropConnector(), services.getContactRepository(), services.getDropMessageRepository());
    }

    private void setTrayIcon(Stage primaryStage) throws ClassNotFoundException, UnsupportedLookAndFeelException, InstantiationException, IllegalAccessException {

        if (!SystemTray.isSupported()) {
            return;
        }

        SystemTray sTray = SystemTray.getSystemTray();
        primaryStage.setOnCloseRequest(arg0 -> primaryStage.hide());
        JPopupMenu popup = buildSystemTrayJPopupMenu(primaryStage);
        URL url = System.class.getResource("/logo-invert_small.png");
        Image img = Toolkit.getDefaultToolkit().getImage(url);
        TrayIcon icon = new TrayIcon(img, "Qabel");

        icon.setImageAutoSize(true);
        trayIconListener(popup, icon);

        try {
            sTray.add(icon);
        } catch (AWTException e) {
            logger.error("failed to add tray icon: " + e.getMessage(), e);
        }

    }

    private void trayIconListener(final JPopupMenu popup, TrayIcon icon) {

        Timer notificationTimer = new Timer();
        notificationTimer.schedule(
            new TimerTask() {
                @Override
                public void run() {
                    if (visible && !inBound) {
                        visible = !visible;
                        popup.setVisible(visible);
                    }
                    inBound = false;
                }
            }, 250, 1500
        );

        icon.addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseMoved(MouseEvent e) {
                inBound = true;
            }
        });

        popup.addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseMoved(MouseEvent e) {
                inBound = true;
            }
        });

        icon.addMouseListener(new MouseAdapter() {

            @Override
            public void mouseReleased(MouseEvent e) {

                if (e.getButton() != MouseEvent.BUTTON1) {
                    return;
                }

                Point point = e.getPoint();
                Rectangle bounds = getScreenViewableBounds(getGraphicsDeviceAt(point));
                int x = point.x;
                int y = point.y;
                if (y < bounds.y) {
                    y = bounds.y;
                } else if (y > bounds.y + bounds.height) {
                    y = bounds.y + bounds.height;
                }
                if (x < bounds.x) {
                    x = bounds.x;
                } else if (x > bounds.x + bounds.width) {
                    x = bounds.x + bounds.width;
                }

                if (x + popup.getWidth() > bounds.x + bounds.width) {
                    x = (bounds.x + bounds.width) - popup.getWidth();
                }
                if (y + popup.getWidth() > bounds.y + bounds.height) {
                    y = (bounds.y + bounds.height) - popup.getHeight();
                }

                visible = !visible;

                if (visible) {
                    popup.setLocation(x, y);
                }
                popup.setVisible(visible);
            }

            @Override
            public void mouseExited(MouseEvent e) {
                visible = false;
                popup.setVisible(visible);
            }
        });

        popup.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseExited(MouseEvent e) {
                if ((e.getX() < popup.getBounds().getMaxX()) &&
                    (e.getX() >= popup.getBounds().getMinX()) &&
                    (e.getY() < popup.getBounds().getMaxY()) &&
                    (e.getY() >= popup.getBounds().getMinY())) {
                    return;
                }
                visible = false;
                popup.setVisible(visible);
            }
        });
    }

    protected JPopupMenu buildSystemTrayJPopupMenu(Stage primaryStage) throws ClassNotFoundException, UnsupportedLookAndFeelException, InstantiationException, IllegalAccessException {
        final JPopupMenu menu = new JPopupMenu();
        final JMenuItem showMenuItem = new JMenuItem("Show");
        final JMenuItem exitMenuItem = new JMenuItem("Exit");

        menu.add(showMenuItem);
        menu.addSeparator();
        menu.add(exitMenuItem);
        showMenuItem.addActionListener(ae -> Platform.runLater(primaryStage::show));
        exitMenuItem.addActionListener(ae -> System.exit(0));

        return menu;
    }

    public GraphicsDevice getGraphicsDeviceAt(Point pos) {

        GraphicsDevice device = null;
        GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
        GraphicsDevice lstGDs[] = ge.getScreenDevices();
        ArrayList<GraphicsDevice> lstDevices = new ArrayList<>(lstGDs.length);

        for (GraphicsDevice gd : lstGDs) {
            GraphicsConfiguration gc = gd.getDefaultConfiguration();
            Rectangle screenBounds = gc.getBounds();
            if (screenBounds.contains(pos)) {
                lstDevices.add(gd);
            }
        }

        if (lstDevices.size() == 1) {
            device = lstDevices.get(0);
        }
        return device;
    }

    public Rectangle getScreenViewableBounds(GraphicsDevice gd) {

        Rectangle bounds = new Rectangle(0, 0, 0, 0);

        if (gd != null) {
            GraphicsConfiguration gc = gd.getDefaultConfiguration();
            bounds = gc.getBounds();
            Insets insets = Toolkit.getDefaultToolkit().getScreenInsets(gc);

            bounds.x += insets.left;
            bounds.y += insets.top;
            bounds.width -= (insets.left + insets.right);
            bounds.height -= (insets.top + insets.bottom);
        }
        return bounds;

    }

}
