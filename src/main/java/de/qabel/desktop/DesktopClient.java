package de.qabel.desktop;

import com.airhacks.afterburner.injection.Injector;
import de.qabel.core.config.Account;
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

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;


public class DesktopClient extends Application {
	private static final String TITLE = "Qabel Desktop Client";
	Scene scene;
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

		boxVolumeFactory = new CachedBoxVolumeFactory(new S3BoxVolumeFactory());
		ClientConfiguration config = initDiContainer();

		SceneAntialiasing aa = SceneAntialiasing.DISABLED;
		primaryStage.getIcons().setAll(new javafx.scene.image.Image(getClass().getResourceAsStream("/logo-invert_small.png")));
		Scene scene;

		view = new LayoutView();
		config.addObserver((o, arg) -> {
			if (arg instanceof Account) {
				Scene layoutScene = new Scene(view.getView(), 800, 600, true, aa);
				Platform.runLater(() -> primaryStage.setScene(layoutScene));
			}
		});


		Platform.setImplicitExit(false);
		primaryStage.setTitle(TITLE);
		scene = new Scene(new LoginView().getView(), 370, 530, true, aa);
		primaryStage.setScene(scene);
		setTrayIcon(primaryStage);

		new Thread(getSyncDaemon(config)).start();

		Runtime.getRuntime().addShutdownHook(new Thread() {
			public void run() {
				Platform.exit();
			}
		});
		primaryStage.show();
	}

	protected SyncDaemon getSyncDaemon(ClientConfiguration config) {

		DefaultTransferManager loadManager = new DefaultTransferManager();
		customProperties.put("loadManager", loadManager);
		new Thread(loadManager, "TransactionManager").start();
		return new SyncDaemon(config.getBoxSyncConfigs(), new DefaultSyncerFactory(boxVolumeFactory, loadManager));
	}

	private ClientConfiguration initDiContainer() throws QblInvalidEncryptionKeyException, URISyntaxException {
		Persistence<String> persistence = new SQLitePersistence(DATABASE_FILE, "qabel".toCharArray(), 65536);
		customProperties.put("persistence", persistence);
		customProperties.put("dropUrlGenerator", new DropUrlGenerator("http://localhost:5000"));
		customProperties.put("boxVolumeFactory", boxVolumeFactory);
		PersistenceIdentityRepository identityRepository = new PersistenceIdentityRepository(persistence);
		customProperties.put("identityRepository", identityRepository);
		PersistenceAccountRepository accountRepository = new PersistenceAccountRepository(persistence);
		customProperties.put("accountRepository", accountRepository);
		PersistenceContactRepository contactRepository = new PersistenceContactRepository(persistence);
		customProperties.put("contactRepository", contactRepository);
		ClientConfiguration clientConfig = getClientConfiguration(
				persistence,
				identityRepository,
				accountRepository
		);
		customProperties.put("clientConfiguration", clientConfig);


		Injector.setConfigurationSource(customProperties::get);
		Injector.setInstanceSupplier(new RecursiveInjectionInstanceSupplier(customProperties));
		return clientConfig;
	}

	private ClientConfiguration getClientConfiguration(
			Persistence<String> persistence,
			IdentityRepository identityRepository,
			AccountRepository accountRepository) {
		ClientConfigurationRepository repo = new PersistenceClientConfigurationRepository(
				persistence,
				new ClientConfigurationFactory(), identityRepository, accountRepository);
		final ClientConfiguration config = repo.load();
		config.addObserver((o, arg) -> {
			repo.save(config);
		});
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
			System.err.println(e);
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
