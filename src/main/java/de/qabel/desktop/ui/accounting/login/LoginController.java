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
import javafx.scene.control.*;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.*;

public class LoginController extends AbstractController implements Initializable {
    private static final Logger logger = LoggerFactory.getLogger(LoginController.class.getSimpleName());

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

    ResourceBundle resourceBundle;
    private ClientConfiguration config;
    private Account account;

    @Inject
    public LoginController(ClientConfiguration config) {
        this.config = config;
    }

    @Inject
    private Stage primaryStage;
    private String accountUrl = "https://accounting.qabel.org";
    Map map = new HashMap<>();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        List<String> providerList = new LinkedList<>();
        providerList.add("https://accounting.qabel.org");
        ObservableList<String> providers = new ObservableListWrapper<>(providerList);
        providerChoices.setItems(providers);
        providerChoices.setValue(accountUrl);

        progressBar.visibleProperty().bind(buttonBar.visibleProperty().not());
        progressBar.managedProperty().bind(progressBar.visibleProperty());
        buttonBar.managedProperty().bind(buttonBar.visibleProperty());
        resourceBundle = resources;


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
    public void newPassword() {
        new Thread(() -> {
            try {
                AccountingHTTP http = createAccount();
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

                AccountingHTTP http = createAccount();
                http.createBoxAccount(email.getText());
                http.login();
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

        // TODO extract login to daemon
        new Thread(() -> {
            try {
                AccountingHTTP http = createAccount();
                http.login();
                config.setAccount(account);

            } catch (URISyntaxException | IOException e) {
                logger.warn(e.getMessage(), e);
                Platform.runLater(() -> toLoginFailureState(e.getMessage()));
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

        AccountingHTTP http = new AccountingHTTP(
                new AccountingServer(
                        new URL(account.getProvider()).toURI(),
                        new URL(account.getProvider()).toURI(),
                        account.getUser(),
                        account.getAuth()
                ),
                new AccountingProfile()
        );

        return http;
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
