package de.qabel.desktop.ui.accounting.login;

import com.sun.javafx.collections.ObservableListWrapper;
import de.qabel.core.accounting.AccountingHTTP;
import de.qabel.core.accounting.AccountingProfile;
import de.qabel.core.config.Account;
import de.qabel.core.config.AccountingServer;
import de.qabel.core.exceptions.QblInvalidCredentials;
import de.qabel.desktop.config.ClientConfiguration;
import de.qabel.desktop.ui.AbstractController;
import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.Pane;

import javax.inject.Inject;
import java.awt.*;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.LinkedList;
import java.util.List;
import java.util.ResourceBundle;

public class LoginController extends AbstractController implements Initializable {
	@FXML
	ChoiceBox<String> providerChoices;

	@FXML
	Button loginButton;

	@FXML
	TextField user;

	@FXML
	TextField auth;

	@FXML
	Pane buttonBar;

	@FXML
	Pane progressBar;

	private ClientConfiguration config;

	@Inject
	public LoginController(ClientConfiguration config) {
		this.config = config;
	}

	@Override
	public void initialize(URL location, ResourceBundle resources) {
		List<String> providerList = new LinkedList<>();
		providerList.add("http://localhost:9696");
		ObservableList<String> providers = new ObservableListWrapper<>(providerList);
		providerChoices.setItems(providers);
		providerChoices.setValue("http://localhost:9696");

		progressBar.visibleProperty().bind(buttonBar.visibleProperty().not());
		progressBar.managedProperty().bind(progressBar.visibleProperty());
		buttonBar.managedProperty().bind(buttonBar.visibleProperty());


		if (config.hasAccount()) {
			Account account = config.getAccount();
			providerChoices.getSelectionModel().select(account.getProvider());
			user.setText(account.getUser());
			auth.setText(account.getAuth());
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

	public void createBoxAccount(ActionEvent actionEvent) {
		new Thread(() -> {
			try {
				URL url = new URL(providerChoices.getValue() + "/api/v0/auth/registration/");
				Desktop desktop = Desktop.isDesktopSupported() ? Desktop.getDesktop() : null;
				if (desktop != null && desktop.isSupported(Desktop.Action.BROWSE)) {
					desktop.browse(url.toURI());
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}).start();
	}

	public void login(ActionEvent actionEvent) {
		login();
	}

	public void login() {
		toProgressState();

		// TODO extract login to daemon
		new Thread(() -> {
			Account account = new Account(null, null, null);
			if (config.hasAccount()) {
				account = config.getAccount();
			}
			account.setProvider(providerChoices.getValue());
			account.setUser(user.getText());
			account.setAuth(auth.getText());
			try {
				AccountingHTTP http = new AccountingHTTP(new AccountingServer(new URL(account.getProvider()).toURI(), account.getUser(), account.getAuth()), new AccountingProfile());
				http.login();
				http.updateProfile();
				config.setAccount(account);
			} catch (URISyntaxException | IOException e) {
				e.printStackTrace();
			} catch (QblInvalidCredentials qblInvalidCredentials) {
				qblInvalidCredentials.printStackTrace();
				Platform.runLater(() -> toFailureState(qblInvalidCredentials.getMessage()));
			}
			Platform.runLater(() -> buttonBar.setVisible(true));
		}).start();
	}

	private void toFailureState(String message) {
		buttonBar.setVisible(true);
		loginButton.getStyleClass().add("error");
		Tooltip t = new Tooltip(message);
		loginButton.setTooltip(t);
	}

	private void toProgressState() {
		buttonBar.setVisible(false);
		loginButton.getStyleClass().remove("error");
	}
}
