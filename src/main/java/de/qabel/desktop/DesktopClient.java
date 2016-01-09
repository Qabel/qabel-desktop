package de.qabel.desktop;

import com.airhacks.afterburner.injection.Injector;
import com.airhacks.afterburner.views.QabelFXMLView;
import de.qabel.core.config.Account;
import de.qabel.core.config.Persistence;
import de.qabel.core.config.SQLitePersistence;
import de.qabel.desktop.config.factory.DropUrlGenerator;
import de.qabel.desktop.repository.persistence.PersistenceAccountRepository;
import de.qabel.desktop.repository.persistence.PersistenceIdentityRepository;
import de.qabel.desktop.ui.LayoutView;
import de.qabel.desktop.ui.accounting.AccountingView;
import de.qabel.desktop.ui.inject.RecursiveInjectionInstanceSupplier;
import javafx.application.Application;
import javafx.application.HostServices;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.SceneAntialiasing;
import javafx.stage.Stage;

import java.awt.*;
import java.awt.event.ActionListener;
import java.net.URL;
import java.util.*;


public class DesktopClient extends Application {
	private static final String TITLE = "Qabel Desktop Client";

	public static void main(String[] args) throws Exception {
		launch(args);
	}

	@Override
	public void start(Stage primaryStage) throws Exception {
		setUserAgentStylesheet(STYLESHEET_MODENA);
		QabelFXMLView.addGlobalCssFileFromResources("/main.css");

		final Map<String, Object> customProperties = new HashMap<>();
		Persistence<String> persistence = new SQLitePersistence("qabel-desktop.sqlite", "qabel".toCharArray(), 65536);
		customProperties.put("persistence", persistence);
		customProperties.put("dropUrlGenerator", new DropUrlGenerator("http://localhost:5000"));
		customProperties.put("identityRepository", new PersistenceIdentityRepository(persistence));
		customProperties.put("accountRepository", new PersistenceAccountRepository(persistence));
		Account account = getBoxAccount();
		customProperties.put("account", account);
		Injector.setConfigurationSource(customProperties::get);
		Injector.setInstanceSupplier(new RecursiveInjectionInstanceSupplier(customProperties));
		setTrayIcon(primaryStage);
		Scene accountingScene = new Scene(new LayoutView().getView(), 800, 600, true, SceneAntialiasing.DISABLED);
		primaryStage.setScene(accountingScene);

		primaryStage.setTitle(TITLE);
		primaryStage.show();
	}

	private void setTrayIcon(Stage primaryStage) {

		SystemTray sTray = SystemTray.getSystemTray();

		ActionListener listenerShow = e -> Platform.runLater(() -> primaryStage.show());
		ActionListener listenerClose = e -> System.exit(0);
		primaryStage.setOnCloseRequest(arg0 -> primaryStage.hide());

		PopupMenu popup = new PopupMenu();
		MenuItem showItem = new MenuItem("Öffnen");
		MenuItem exitItem = new MenuItem("Beenden");

		showItem.addActionListener(listenerShow);
		exitItem.addActionListener(listenerClose);

		popup.add(showItem);
		popup.add(exitItem);

		URL url = System.class.getResource("/logo.png");
		Image img = Toolkit.getDefaultToolkit().getImage(url);
		TrayIcon icon = new TrayIcon(img, "Qabel", popup);
		icon.setImageAutoSize(true);

		try {
			sTray.add(icon);
		} catch (AWTException e) {
			System.err.println(e);
		}

	}

	public static Account getBoxAccount() {
		return new Account("http://localhost:9696", "testuser", "testuser");
	}

}
