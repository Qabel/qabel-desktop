package de.qabel.desktop.ui.accounting.login;

import com.sun.javafx.collections.ObservableListWrapper;
import de.qabel.core.accounting.AccountingProfile;
import de.qabel.core.accounting.BoxClient;
import de.qabel.core.accounting.BoxHttpClient;
import de.qabel.core.config.Account;
import de.qabel.core.config.AccountingServer;
import de.qabel.core.exceptions.QblCreateAccountFailException;
import de.qabel.core.exceptions.QblInvalidCredentials;
import de.qabel.core.repository.Transaction;
import de.qabel.core.repository.TransactionManager;
import de.qabel.desktop.config.ClientConfig;
import de.qabel.desktop.ui.AbstractController;
import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.*;
import java.util.function.Function;

public class LoginController extends AbstractController implements Initializable {
    private static final Logger logger = LoggerFactory.getLogger(LoginController.class);

    @FXML
    ChoiceBox<String> providerChoices;

    @FXML
    Button loginButton;

    @FXML
    Button openCreateButton;
    @FXML
    Button newPassword;

    @FXML
    Button recoverPassword;
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

    private ResourceBundle resourceBundle;
    private ClientConfig config;
    private Account account;
    AccountingServer server;
    private BoxClient http;
    Function<AccountingServer, BoxClient> boxClientFactory
        = server -> new BoxHttpClient(server, new AccountingProfile());

    @Inject
    public LoginController(ClientConfig config) {
        this.config = config;
    }

    @Inject
    private Stage primaryStage;

    @Inject
    private TransactionManager transactionManager;

    @Inject
    private URI accountingUri;

    Map map = new HashMap<>();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        List<String> providerList = new LinkedList<>();
        providerList.add(accountingUri.toString());
        ObservableList<String> providers = new ObservableListWrapper<>(providerList);
        providerChoices.setItems(providers);
        providerChoices.setValue(accountingUri.toString());

        progressBar.visibleProperty().bind(buttonBar.visibleProperty().not());
        progressBar.managedProperty().bind(progressBar.visibleProperty());
        buttonBar.managedProperty().bind(buttonBar.visibleProperty());
        resourceBundle = resources;

        confirm.visibleProperty().bindBidirectional(confirm.managedProperty());
        email.visibleProperty().bindBidirectional(email.managedProperty());

        if (config.hasAccount()) {
            Account account = config.getAccount();
            user.setText(account.getUser());
            password.setText(account.getAuth());
            if (providerChoices.getItems().contains(config.getAccount().getProvider())) {
                providerChoices.getSelectionModel().select(account.getProvider());
                Platform.runLater(this::login);
            }
        }
    }


    @FXML
    public void handleEnterPressed(KeyEvent event) {
        if (event.getCode() == KeyCode.ENTER) {
            login();
        }
    }

    @FXML
    public void newPassword() {
        new Thread(() -> {
            try {
                BoxClient http = createAccount();
                http.resetPassword(email.getText());
                toMailSend(resourceBundle.getString("loginSend"));
                newPassword.disableProperty().set(true);

            } catch (URISyntaxException | IOException | IllegalArgumentException e) {
                Platform.runLater(() -> toEMailSendFailureState(resourceBundle.getString("loginSendFail")));
            }
        }).start();
    }

    @FXML
    public void recoverPassword() {

        Platform.runLater(() -> {
            email.setManaged(true);

            recoverPassword.setManaged(false);
            recoverPassword.setVisible(false);

            newPassword.setManaged(true);
            primaryStage.setHeight(700);
        });

    }

    @FXML
    public void openCreateBoxAccountSetup() {
        Platform.runLater(() -> {
            email.setManaged(true);
            confirm.setManaged(true);
            createButton.setManaged(true);

            openCreateButton.setManaged(false);
            openCreateButton.setVisible(false);

            primaryStage.setHeight(700);

        });
    }

    @FXML
    public void createBoxAccount() {
        toProgressState();

        new Thread(() -> {
            try {
                if (!password.getText().equals(confirm.getText())) {
                    String text = "Passwords not equals";
                    map.put("password1", text);
                    createFailureState(text);
                    throw new IllegalArgumentException(text);
                }

                BoxClient http = createAccount();
                http.createBoxAccount(email.getText());
                http.login();
                config.setAccount(account);

            } catch (QblCreateAccountFailException e) {
                logger.info("failed to create account: " + e.getMessage());
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
            } catch (QblInvalidCredentials | IllegalArgumentException e) {
                createFailureState(e.getMessage());
            } catch (URISyntaxException | IOException e) {
                alert("Failed to create box account", e);
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

    @FXML
    public void login() {
        toProgressState();

        new Thread(() -> {
            try {
                http = createAccount();
                if (account.getToken() == null) {
                    http.login();
                    account.setToken(server.getAuthToken());
                }

                try (Transaction ignored = transactionManager.beginTransaction()) {
                    config.setAccount(account);
                }

            } catch (URISyntaxException | IOException e) {
                logger.warn(e.getMessage(), e);
                Platform.runLater(() -> toLoginFailureState(e.getMessage()));
            } catch (QblInvalidCredentials e) {
                logger.info("invalid credentials: " + e.getMessage());
                Platform.runLater(() -> toLoginFailureState(e.getMessage()));
            } catch (Exception e) {
                logger.warn(e.getMessage(), e);
                Platform.runLater(() -> toLoginFailureState(e.getMessage()));
            }
            Platform.runLater(() -> buttonBar.setVisible(true));
        }).start();
    }

    private BoxClient createAccount() throws MalformedURLException, URISyntaxException {
        account = new Account(null, null, null);
        if (config.hasAccount()) {
            account = config.getAccount();
        }
        account.setProvider(providerChoices.getValue());
        account.setUser(user.getText());
        account.setAuth(password.getText());

        server = new AccountingServer(
            new URL(account.getProvider()).toURI(),
            new URL(account.getProvider()).toURI(),
            account.getUser(),
            account.getAuth()
        );
        if (account.getToken() != null) {
            server.setAuthToken(account.getToken());
        }
        return boxClientFactory.apply(server);
    }

    private void toLoginFailureState(String message) {
        buttonBar.setVisible(true);
        showError(message, loginButton);
    }

    private void showError(String message, Button button) {
        button.getStyleClass().add("error");
        Tooltip t = new Tooltip(message);
        button.setTooltip(t);
    }

    private void toCreateFailureState(String message) {
        showError(message, createButton);
    }

    private void toEMailSendFailureState(String message) {
        showError(message, newPassword);
    }

    private void toMailSend(String message) {
        newPassword.getStyleClass().add("check");
        Tooltip t = new Tooltip(message);
        newPassword.setTooltip(t);
    }

    private void toProgressState() {
        buttonBar.setVisible(false);
        loginButton.getStyleClass().remove("error");
    }
}
