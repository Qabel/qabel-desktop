package de.qabel.desktop;

import com.airhacks.afterburner.injection.Injector;
import de.qabel.core.config.*;
import de.qabel.core.exceptions.QblInvalidEncryptionKeyException;
import de.qabel.desktop.config.ClientConfiguration;
import de.qabel.desktop.config.factory.ClientConfigurationFactory;
import de.qabel.desktop.config.factory.DropUrlGenerator;
import de.qabel.desktop.config.factory.S3BoxVolumeFactory;
import de.qabel.desktop.daemon.drop.DropDaemon;
import de.qabel.desktop.config.factory.*;
import de.qabel.desktop.daemon.management.DefaultTransferManager;
import de.qabel.desktop.daemon.management.MonitoredTransferManager;
import de.qabel.desktop.daemon.management.TransferManager;
import de.qabel.desktop.daemon.sync.SyncDaemon;
import de.qabel.desktop.daemon.sync.worker.DefaultSyncerFactory;
import de.qabel.desktop.repository.AccountRepository;
import de.qabel.desktop.repository.ClientConfigurationRepository;
import de.qabel.desktop.repository.IdentityRepository;
import de.qabel.desktop.repository.exception.EntityNotFoundExcepion;
import de.qabel.desktop.repository.exception.PersistenceException;
import de.qabel.desktop.repository.persistence.*;
import de.qabel.desktop.ui.LayoutView;
import de.qabel.desktop.ui.accounting.login.LoginView;
import de.qabel.desktop.ui.connector.HttpDropConnector;
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
import java.net.URISyntaxException;
import java.net.URL;
import java.util.*;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;


public class DesktopClient extends Application {
	private static final Logger logger = LoggerFactory.getLogger(DesktopClient.class.getSimpleName());
	private static final String TITLE = "Qabel Desktop Client";
	private static String DATABASE_FILE = "db.sqlite";
	private final Map<String, Object> customProperties = new HashMap<>();
	private LayoutView view;
	private HttpDropConnector httpDropConnector;
	private Date lastDate = null;
	private PersistenceDropMessageRepository dropMessageRepository;
	private PersistenceContactRepository contactRepository;
	private BoxVolumeFactory boxVolumeFactory;
	private Stage primaryStage;
	private MonitoredTransferManager transferManager;

	public static void main(String[] args) throws Exception {
		if (args.length > 0) {
			DATABASE_FILE = args[0];
		}
		UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
		launch(args);
	}

	@Override
	public void start(Stage stage) throws Exception {
		primaryStage = stage;
		setUserAgentStylesheet(STYLESHEET_MODENA);

		ClientConfiguration config = initDiContainer();

		SceneAntialiasing aa = SceneAntialiasing.BALANCED;
		primaryStage.getIcons().setAll(new javafx.scene.image.Image(getClass().getResourceAsStream("/logo-invert_small.png")));
		Scene scene;


		Platform.setImplicitExit(false);
		primaryStage.setTitle(TITLE);
		scene = new Scene(new LoginView().getView(), 370, 550, true, aa);
		primaryStage.setScene(scene);

		config.addObserver((o, arg) -> {
			Platform.runLater(() -> {
				if (arg instanceof Account) {
					new Thread(getSyncDaemon(config)).start();
					try {
						new Thread(getDropDaemon(config)).start();
					} catch (PersistenceException | EntityNotFoundExcepion e) {
						e.printStackTrace();
					}
					view = new LayoutView();
					Scene layoutScene = new Scene(view.getView(), 800, 600, true, aa);
					Platform.runLater(() -> primaryStage.setScene(layoutScene));
				}
			});
		});

		setTrayIcon(primaryStage);

		Runtime.getRuntime().addShutdownHook(new Thread() {
			public void run() {
				Platform.exit();
			}
		});
		primaryStage.show();
	}

	protected SyncDaemon getSyncDaemon(ClientConfiguration config) {
		new Thread(transferManager, "TransactionManager").start();
		return new SyncDaemon(config.getBoxSyncConfigs(), new DefaultSyncerFactory(boxVolumeFactory, transferManager));
	}

	protected DropDaemon getDropDaemon(ClientConfiguration config) throws PersistenceException, EntityNotFoundExcepion {
		return new DropDaemon(config,httpDropConnector,contactRepository, dropMessageRepository);
	}

	private ClientConfiguration initDiContainer() throws QblInvalidEncryptionKeyException, URISyntaxException {
		Persistence<String> persistence = new SQLitePersistence(DATABASE_FILE);
		transferManager = new MonitoredTransferManager(new DefaultTransferManager());
		customProperties.put("loadManager", transferManager);
		customProperties.put("transferManager", transferManager);
		customProperties.put("persistence", persistence);
		customProperties.put("dropUrlGenerator", new DropUrlGenerator("https://qdrop.prae.me"));
		customProperties.put("boxVolumeFactory", boxVolumeFactory);
		PersistenceIdentityRepository identityRepository = new PersistenceIdentityRepository(persistence);
		customProperties.put("identityRepository", identityRepository);
		PersistenceAccountRepository accountRepository = new PersistenceAccountRepository(persistence);
		customProperties.put("accountRepository", accountRepository);
		contactRepository = new PersistenceContactRepository(persistence);
		customProperties.put("contactRepository", contactRepository);
		dropMessageRepository = new PersistenceDropMessageRepository(persistence);
		customProperties.put("dropMessageRepository", dropMessageRepository);
		httpDropConnector = new HttpDropConnector();
		customProperties.put("httpDropConnector", httpDropConnector);
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
