package de.qabel.desktop;

import com.airhacks.afterburner.injection.Injector;
import de.qabel.core.accounting.AccountingHTTP;
import de.qabel.core.accounting.AccountingProfile;
import de.qabel.core.config.Account;
import de.qabel.core.config.AccountingServer;
import de.qabel.core.config.Persistence;
import de.qabel.core.config.SQLitePersistence;
import de.qabel.core.exceptions.QblInvalidEncryptionKeyException;
import de.qabel.desktop.config.ClientConfiguration;
import de.qabel.desktop.config.factory.*;
import de.qabel.desktop.daemon.management.DefaultTransferManager;
import de.qabel.desktop.daemon.sync.SyncDaemon;
import de.qabel.desktop.daemon.sync.worker.DefaultSyncerFactory;
import de.qabel.desktop.repository.AccountRepository;
import de.qabel.desktop.repository.ClientConfigurationRepository;
import de.qabel.desktop.repository.IdentityRepository;
import de.qabel.desktop.repository.persistence.PersistenceAccountRepository;
import de.qabel.desktop.repository.persistence.PersistenceClientConfigurationRepository;
import de.qabel.desktop.repository.persistence.PersistenceContactRepository;
import de.qabel.desktop.repository.persistence.PersistenceIdentityRepository;
import de.qabel.desktop.ui.LayoutView;
import de.qabel.desktop.ui.accounting.login.LoginView;
import de.qabel.desktop.ui.inject.RecursiveInjectionInstanceSupplier;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.SceneAntialiasing;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;


public class DesktopClient extends Application {
	private static final Logger logger = LoggerFactory.getLogger(DesktopClient.class.getSimpleName());
	private static final String TITLE = "Qabel Desktop Client";
	private static String DATABASE_FILE = "db.sqlite";
	private final Map<String, Object> customProperties = new HashMap<>();
	private LayoutView view;
	private BoxVolumeFactory boxVolumeFactory;

	public static void main(String[] args) throws Exception {
		if (args.length > 0) {
			DATABASE_FILE = args[0];
		}
		UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
		launch(args);
	}

	@Override
	public void start(Stage primaryStage) throws Exception {
		setUserAgentStylesheet(STYLESHEET_MODENA);

		ClientConfiguration config = initDiContainer();

		SceneAntialiasing aa = SceneAntialiasing.DISABLED;
		primaryStage.getIcons().setAll(new javafx.scene.image.Image(getClass().getResourceAsStream("/logo-invert_small.png")));
		Scene scene;

		view = new LayoutView();
		final boolean[] initialized = {false};
		config.addObserver((o, arg) -> {
			if (arg instanceof Account) {
				if (initialized[0]) {
					return;
				}
				initialized[0] = true;
				try {
					if (!config.hasDeviceId()) {
						config.setDeviceId(generateDeviceId());
					}
					String deviceId = config.getDeviceId();
					boxVolumeFactory = new CachedBoxVolumeFactory(new S3BoxVolumeFactory(deviceId));
					customProperties.put("boxVolumeFactory", boxVolumeFactory);
					new Thread(getSyncDaemon(config)).start();
					view.getViewAsync((parent) -> {
						Scene layoutScene = new Scene(parent, 800, 600, true, aa);
						Platform.runLater(() -> primaryStage.setScene(layoutScene));
					});
				} catch (Exception e) {
					throw new IllegalArgumentException("invalid account: " + e.getMessage(), e);
				}
			}
		});

		Platform.setImplicitExit(false);
		primaryStage.setTitle(TITLE);
		scene = new Scene(new LoginView().getView(), 370, 530, true, aa);
		primaryStage.setScene(scene);
		setTrayIcon(primaryStage);


		Runtime.getRuntime().addShutdownHook(new Thread() {
			public void run() {
				Platform.exit();
			}
		});
		primaryStage.show();
	}

	private AccountingHTTP getAccountingHttp(Account account) throws MalformedURLException, URISyntaxException {
		return new AccountingHTTP(
				new AccountingServer(
						new URL(account.getProvider()).toURI(),
						account.getUser(),
						account.getAuth()
				),
				new AccountingProfile()
		);
	}

	protected SyncDaemon getSyncDaemon(ClientConfiguration config) {
		DefaultTransferManager transferManager = new DefaultTransferManager();
		customProperties.put("loadManager", transferManager);
		customProperties.put("transferManager", transferManager);
		new Thread(transferManager, "TransactionManager").start();
		return new SyncDaemon(config.getBoxSyncConfigs(), new DefaultSyncerFactory(boxVolumeFactory, transferManager));
	}

	private ClientConfiguration initDiContainer() throws QblInvalidEncryptionKeyException, URISyntaxException {
		Persistence<String> persistence = new SQLitePersistence(DATABASE_FILE, "qabel".toCharArray(), 65536);
		customProperties.put("persistence", persistence);
		customProperties.put("dropUrlGenerator", new DropUrlGenerator("http://localhost:5000"));
		PersistenceIdentityRepository identityRepository = new PersistenceIdentityRepository(persistence);
		customProperties.put("identityRepository", identityRepository);
		PersistenceAccountRepository accountRepository = new PersistenceAccountRepository(persistence);
		customProperties.put("accountRepository", accountRepository);
		ClientConfiguration clientConfig = getClientConfiguration(
				persistence,
				identityRepository,
				accountRepository
		);
		if (!clientConfig.hasDeviceId()) {
			clientConfig.setDeviceId(generateDeviceId());
		}
		boxVolumeFactory = new CachedBoxVolumeFactory(new S3BoxVolumeFactory(clientConfig.getDeviceId()));
		customProperties.put("boxVolumeFactory", boxVolumeFactory);
		PersistenceContactRepository contactRepository = new PersistenceContactRepository(persistence);
		customProperties.put("contactRepository", contactRepository);
		customProperties.put("clientConfiguration", clientConfig);


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
			AccountRepository accountRepository) {
		ClientConfigurationRepository repo = new PersistenceClientConfigurationRepository(
				persistence,
				new ClientConfigurationFactory(), identityRepository, accountRepository);
		final ClientConfiguration config = repo.load();
		config.addObserver((o, arg) -> repo.save(config));
		return config;
	}

	private void setTrayIcon(Stage primaryStage) throws ClassNotFoundException, UnsupportedLookAndFeelException, InstantiationException, IllegalAccessException {

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
		icon.addMouseListener(new MouseAdapter() {
			boolean visible = false;

			@Override
			public void mouseReleased(MouseEvent e) {
				if (e.getButton() != MouseEvent.BUTTON1) {
					return;
				}
				popup.setLocation(e.getX(), e.getY());
				visible = !visible;
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
}
