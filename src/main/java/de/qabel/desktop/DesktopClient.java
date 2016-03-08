package de.qabel.desktop;

import com.airhacks.afterburner.injection.Injector;
import com.airhacks.afterburner.views.QabelFXMLView;
import de.qabel.core.accounting.AccountingHTTP;
import de.qabel.core.accounting.AccountingProfile;
import de.qabel.core.config.*;
import de.qabel.core.exceptions.QblInvalidEncryptionKeyException;
import de.qabel.core.http.DropHTTP;
import de.qabel.desktop.config.ClientConfiguration;
import de.qabel.desktop.config.factory.*;
import de.qabel.desktop.crashReports.HockeyApp;
import de.qabel.desktop.daemon.NetworkStatus;
import de.qabel.desktop.daemon.drop.DropDaemon;
import de.qabel.desktop.daemon.management.DefaultTransferManager;
import de.qabel.desktop.daemon.management.MonitoredTransferManager;
import de.qabel.desktop.daemon.share.ShareNotificationHandler;
import de.qabel.desktop.daemon.sync.SyncDaemon;
import de.qabel.desktop.daemon.sync.worker.DefaultSyncerFactory;
import de.qabel.desktop.repository.AccountRepository;
import de.qabel.desktop.repository.ClientConfigurationRepository;
import de.qabel.desktop.repository.DropMessageRepository;
import de.qabel.desktop.repository.IdentityRepository;
import de.qabel.desktop.repository.exception.EntityNotFoundExcepion;
import de.qabel.desktop.repository.exception.PersistenceException;
import de.qabel.desktop.repository.persistence.*;
import de.qabel.desktop.ui.LayoutView;
import de.qabel.desktop.ui.accounting.login.LoginView;
import de.qabel.desktop.ui.actionlog.item.renderer.MessageRendererFactory;
import de.qabel.desktop.ui.actionlog.item.renderer.PlaintextMessageRenderer;
import de.qabel.desktop.ui.actionlog.item.renderer.ShareNotificationRenderer;
import de.qabel.desktop.ui.connector.HttpDropConnector;
import de.qabel.desktop.ui.inject.RecursiveInjectionInstanceSupplier;
import de.qabel.desktop.update.AppInfos;
import de.qabel.desktop.update.HttpUpdateChecker;
import de.qabel.desktop.update.LatestVersionInfo;
import de.qabel.desktop.update.VersionInformation;
import javafx.application.Application;
import javafx.application.HostServices;
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
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.Timer;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static javafx.scene.control.Alert.AlertType.INFORMATION;
import static javafx.scene.control.Alert.AlertType.WARNING;


public class DesktopClient extends Application {
	private static final Logger logger = LoggerFactory.getLogger(DesktopClient.class.getSimpleName());
	private static final String TITLE = "Qabel Desktop Client";
	private static Path DATABASE_FILE = Paths.get(System.getProperty("user.home")).resolve(".qabel/db.sqlite");
	private final Map<String, Object> customProperties = new HashMap<>();
	private boolean inBound;
	private LayoutView view;
	private HttpDropConnector dropConnector;
	private PersistenceDropMessageRepository dropMessageRepository;
	private PersistenceContactRepository contactRepository;
	private BoxVolumeFactory boxVolumeFactory;
	private Stage primaryStage;
	private MonitoredTransferManager transferManager;
	private MessageRendererFactory rendererFactory = new MessageRendererFactory();
	private BlockSharingService sharingService;
	private ExecutorService executorService = Executors.newCachedThreadPool();
	private ClientConfiguration config;
	private boolean visible = false;
	private PersistenceIdentityRepository identityRepository;
	private NetworkStatus networkStatus;


	public static void main(String[] args) throws Exception {

		if (args.length > 0) {
			DATABASE_FILE = new File(args[0]).getAbsoluteFile().toPath();
		}
		UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
		launch(args);
	}

	private void checkVersion() {
		try {
			HttpUpdateChecker checker = new HttpUpdateChecker();
			String currentVersion = IOUtils.toString(DesktopClient.class.getResourceAsStream("/version"));

			if (currentVersion.equals("dev")) {
				return;
			}

			if (!checker.isCurrent(currentVersion)) {
				final boolean required = !checker.isAllowed(currentVersion);

				ResourceBundle resources = QabelFXMLView.getDefaultResourceBundle();
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
		config = initDiContainer();

		SceneAntialiasing aa = SceneAntialiasing.BALANCED;
		primaryStage.getIcons().setAll(new javafx.scene.image.Image(getClass().getResourceAsStream("/logo-invert_small.png")));
		Scene scene;


		Platform.setImplicitExit(false);
		primaryStage.setTitle(TITLE);
		scene = new Scene(new LoginView().getView(), 370, 570, true, aa);
		primaryStage.setScene(scene);

		config.addObserver((o, arg) -> {
			Platform.runLater(() -> {
				if (arg instanceof Account) {
					try {
						ClientConfiguration configuration = (ClientConfiguration) customProperties.get("clientConfiguration");
						Account acc = (Account) arg;
						AccountingServer server = new AccountingServer(new URI(acc.getProvider()), acc.getUser(), acc.getAuth());
						AccountingHTTP accountingHTTP = new AccountingHTTP(server, new AccountingProfile());

						BoxVolumeFactory factory = new BlockBoxVolumeFactory(
								configuration.getDeviceId().getBytes(),
								accountingHTTP,
								identityRepository
						);
						boxVolumeFactory = new CachedBoxVolumeFactory(factory);
						customProperties.put("boxVolumeFactory", boxVolumeFactory);
						sharingService = new BlockSharingService(dropMessageRepository, dropConnector);
						customProperties.put("sharingService", sharingService);

						new Thread(getSyncDaemon(config)).start();
						new Thread(getDropDaemon(config)).start();
						view = new LayoutView();
						Parent view = this.view.getView();
						Scene layoutScene = new Scene(view, 800, 600, true, aa);
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

		dropMessageRepository.addObserver(new ShareNotificationHandler(config));

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
			ShareNotificationRenderer renderer = new ShareNotificationRenderer(((BoxVolumeFactory) customProperties.get("boxVolumeFactory")).getVolume(config.getAccount(), arg).getReadBackend(), sharingService);
			rendererFactory.addRenderer(DropMessageRepository.PAYLOAD_TYPE_SHARE_NOTIFICATION, renderer);
		});
	}

	protected SyncDaemon getSyncDaemon(ClientConfiguration config) throws IOException {
		new Thread(transferManager, "TransactionManager").start();
		return new SyncDaemon(config.getBoxSyncConfigs(), new DefaultSyncerFactory(boxVolumeFactory, transferManager));
	}

	protected DropDaemon getDropDaemon(ClientConfiguration config) throws PersistenceException, EntityNotFoundExcepion {
		return new DropDaemon(config, dropConnector, contactRepository, dropMessageRepository);
	}

	private ClientConfiguration initDiContainer() throws Exception {
		if (!Files.exists(DATABASE_FILE) && !Files.exists(DATABASE_FILE.getParent())) {
			Files.createDirectories(DATABASE_FILE.getParent());
		}

		Persistence<String> persistence = new SQLitePersistence(DATABASE_FILE.toFile().getAbsolutePath());
		transferManager = new MonitoredTransferManager(new DefaultTransferManager());
		customProperties.put("loadManager", transferManager);
		customProperties.put("transferManager", transferManager);
		customProperties.put("persistence", persistence);
		customProperties.put("dropUrlGenerator", new DropUrlGenerator("https://qdrop.prae.me"));
		identityRepository = new PersistenceIdentityRepository(persistence);
		customProperties.put("identityRepository", identityRepository);
		PersistenceAccountRepository accountRepository = new PersistenceAccountRepository(persistence);
		customProperties.put("accountRepository", accountRepository);
		contactRepository = new PersistenceContactRepository(persistence);
		customProperties.put("contactRepository", contactRepository);
		dropMessageRepository = new PersistenceDropMessageRepository(persistence);
		customProperties.put("dropMessageRepository", dropMessageRepository);

		networkStatus = new NetworkStatus();
		customProperties.put("networkStatus", networkStatus);
		dropConnector = new HttpDropConnector(networkStatus, new DropHTTP());
		customProperties.put("dropConnector", dropConnector);
		customProperties.put("reportHandler", new HockeyApp());
		ClientConfiguration clientConfig = getClientConfiguration(
				persistence,
				identityRepository,
				accountRepository
		);
		if (!clientConfig.hasDeviceId()) {
			clientConfig.setDeviceId(generateDeviceId());
		}
		customProperties.put("contactRepository", contactRepository);
		customProperties.put("clientConfiguration", clientConfig);
		customProperties.put("primaryStage", primaryStage);

		rendererFactory.setFallbackRenderer(new PlaintextMessageRenderer());
		customProperties.put("messageRendererFactory", rendererFactory);

		Injector.setConfigurationSource(customProperties::get);
		Injector.setInstanceSupplier(new RecursiveInjectionInstanceSupplier(customProperties));
		return clientConfig;
	}

	private String generateDeviceId() {
		return UUID.randomUUID().toString();
	}

	private ClientConfiguration getClientConfiguration(
			Persistence<String> persistence,
			IdentityRepository identityRepository,
			AccountRepository accountRepository
	) {
		ClientConfigurationRepository repo = new PersistenceClientConfigurationRepository(
				persistence,
				new ClientConfigurationFactory(), identityRepository, accountRepository);
		final ClientConfiguration config = repo.load();
		config.addObserver((o, arg) -> repo.save(config));
		return config;
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
