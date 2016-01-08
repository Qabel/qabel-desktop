package de.qabel.desktop;

import com.airhacks.afterburner.injection.Injector;
import de.qabel.core.config.Account;
import de.qabel.core.config.Persistence;
import de.qabel.core.config.SQLitePersistence;
import de.qabel.desktop.config.factory.DropUrlGenerator;
import de.qabel.desktop.repository.persistence.PersistenceAccountRepository;
import de.qabel.desktop.repository.persistence.PersistenceIdentityRepository;
import de.qabel.desktop.ui.accounting.AccountingView;
import de.qabel.desktop.ui.inject.RecursiveInjectionInstanceSupplier;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.util.*;


public class DesktopClient extends Application {
	private static final String TITLE = "Qabel Desktop Client";

	public static void main(String[] args) throws Exception {
		launch(args);
	}

	@Override
	public void start(Stage primaryStage) throws Exception {
		setUserAgentStylesheet(STYLESHEET_MODENA);

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

		Scene accountingScene = new Scene(new AccountingView().getView(), 800, 600);
		primaryStage.setScene(accountingScene);

		primaryStage.setTitle(TITLE);
		primaryStage.show();
	}

	public static Account getBoxAccount() {
		return new Account("http://localhost:9696", "testuser", "testuser");
	}

}
