package de.qabel.desktop.ui.accounting.login;

import com.sun.javafx.collections.ObservableListWrapper;
import de.qabel.core.accounting.AccountingHTTP;
import de.qabel.core.accounting.AccountingProfile;
import de.qabel.core.config.Account;
import de.qabel.core.config.AccountingServer;
import de.qabel.core.exceptions.QblCreateAccountFailException;
import de.qabel.core.exceptions.QblInvalidCredentials;
import de.qabel.desktop.config.ClientConfiguration;
import de.qabel.desktop.ui.AbstractController;
import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.*;
import javafx.scene.control.TextField;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;

import javax.inject.Inject;
import java.awt.*;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.*;
import java.util.List;

public class LoginController extends AbstractController implements Initializable {
	@FXML
	ChoiceBox<String> providerChoices;

	@FXML
	Button loginButton;

	@FXML
	Button openCreateButton;
	@FXML
	Button createButton;


	@FXML
	TextField user;

	@FXML
	PasswordField password;

	@FXML
	PasswordField confirm;

	@FXML
	TextField email;


	@FXML
	Pane buttonBar;

	@FXML
	Pane progressBar;


	private ClientConfiguration config;
	private Account account;
	@Inject
	public LoginController(ClientConfiguration config) {
		this.config = config;
	}

	@Inject
	private Stage primaryStage;
	private String accountUrl = "http://localhost:9696";
	Map map = new HashMap<>();

	@Override
	public void initialize(URL location, ResourceBundle resources) {
		List<String> providerList = new LinkedList<>();
		providerList.add(accountUrl);
		ObservableList<String> providers = new ObservableListWrapper<>(providerList);
		providerChoices.setItems(providers);
		providerChoices.setValue(accountUrl);

		progressBar.visibleProperty().bind(buttonBar.visibleProperty().not());
		progressBar.managedProperty().bind(progressBar.visibleProperty());
		buttonBar.managedProperty().bind(buttonBar.visibleProperty());


		if (config.hasAccount()) {
			Account account = config.getAccount();
			providerChoices.getSelectionModel().select(account.getProvider());
			user.setText(account.getUser());
			password.setText(account.getAuth());
			Platform.runLater(this::login);
		}
	}

	public void recoverPassword(ActionEvent actionEvent) {
		new Thread(() -> {
			try {
				URL url = new URL(providerChoices.getValue() + "/accounts/password_reset/");
				Desktop desktop = Desktop.isDesktopSupported() ? Desktop.getDesktop() : null;
				if (desktop != null && desktop.isSupported(Desktop.Action.BROWSE)) {
					desktop.browse(url.toURI());
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}).start();
	}

	public void openCreateBoxAccountSetup(ActionEvent actionEvent) {
		openCreateBoxAccountSetup();
	}

	private void openCreateBoxAccountSetup() {
		Platform.runLater(() -> {
			email.setManaged(true);
			confirm.setManaged(true);
			createButton.setManaged(true);

			openCreateButton.setManaged(false);
			primaryStage.setHeight(650);
		});
	}


	public void createBoxAccount(ActionEvent actionEvent) {
		createBoxAccount();
	}

	private void createBoxAccount() {
		toProgressState();



		new Thread(() -> {
			try {
				if (!password.getText().equals(confirm.getText())) {
					String text = "Passwords not equals";
					map.put("password1", text);
					createFailureState(text);
					throw new IllegalArgumentException(text);
				}

				AccountingHTTP http = createAccount();
				http.createBoxAccount(email.getText());
				http.login();
				http.updateProfile();
				config.setAccount(account);

			} catch (QblCreateAccountFailException e) {
				e.printStackTrace();
				map = e.getMap();
				String text = "";
				if (map.containsKey("email")) {
					text = text.concat(((ArrayList) map.get("email")).get(0) + " ");
				}
				if (map.containsKey("password1")) {
					text = text.concat(((ArrayList) map.get("password1")).get(0) + " ");
				}
				if (map.containsKey("username")) {
					text = text.concat(((ArrayList) map.get("username")).get(0) + " ");
				}
				createFailureState(text);
			} catch (QblInvalidCredentials | URISyntaxException | IOException | IllegalArgumentException e) {
				e.printStackTrace();
			} finally {
				Platform.runLater(() -> buttonBar.setVisible(true));
			}
		}).start();
	}

	private void createFailureState(String text) {
		final String finalText = text;
		Platform.runLater(() ->
				toCreateFailureState(finalText));
	}

	public void login(ActionEvent actionEvent) {
		login();
	}

	public void login() {
		toProgressState();

		// TODO extract login to daemon
		new Thread(() -> {
			try {
				AccountingHTTP http = createAccount();
				http.login();
				http.updateProfile();
				config.setAccount(account);

			} catch (URISyntaxException | IOException e) {
				e.printStackTrace();
			} catch (QblInvalidCredentials qblInvalidCredentials) {
				qblInvalidCredentials.printStackTrace();
				Platform.runLater(() -> toLoginFailureState(qblInvalidCredentials.getMessage()));
			}
			Platform.runLater(() -> buttonBar.setVisible(true));
		}).start();
	}

	private AccountingHTTP createAccount() throws MalformedURLException, URISyntaxException {
		account = new Account(null, null, null);
		if (config.hasAccount()) {
			account = config.getAccount();
		}
		account.setProvider(providerChoices.getValue());
		account.setUser(user.getText());
		account.setAuth(password.getText());

		AccountingHTTP http = new AccountingHTTP(new AccountingServer(new URL(account.getProvider()).toURI(), account.getUser(), account.getAuth()), new AccountingProfile());

		return http;
	}

	private void toLoginFailureState(String message) {
		buttonBar.setVisible(true);
		loginButton.getStyleClass().add("error");
		Tooltip t = new Tooltip(message);
		loginButton.setTooltip(t);
	}

	private void toCreateFailureState(String message) {
		createButton.getStyleClass().add("error");
		Tooltip t = new Tooltip(message);
		createButton.setTooltip(t);
	}

	private void toProgressState() {
		buttonBar.setVisible(false);
		loginButton.getStyleClass().remove("error");
	}
}
