package de.qabel.desktop.ui.contact.index;

import com.google.i18n.phonenumbers.NumberParseException;
import com.jfoenix.controls.JFXTextField;
import de.qabel.core.config.Contact;
import de.qabel.core.index.IndexService;
import de.qabel.desktop.ui.AbstractController;
import de.qabel.desktop.ui.contact.index.result.IndexSearchResultItemView;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.BooleanBinding;
import javafx.beans.binding.StringBinding;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rx.Observable;
import rx.schedulers.Schedulers;

import javax.inject.Inject;
import java.net.URL;
import java.util.Collections;
import java.util.List;
import java.util.ResourceBundle;
import java.util.concurrent.TimeUnit;

import static de.qabel.core.index.PhoneUtilsKt.formatPhoneNumber;
import static de.qabel.core.index.PhoneUtilsKt.isValidPhoneNumber;

public class IndexSearchController extends AbstractController implements Initializable {
    private static final Logger logger = LoggerFactory.getLogger(IndexSearchController.class);
    @FXML
    public Pane resultContainer;
    @FXML
    public HBox loader;
    @FXML
    private Pane indexSearchRoot;
    @FXML
    JFXTextField search;
    @FXML
    Label emailSearch;
    @FXML
    Label phoneSearch;

    @Inject
    private IndexService indexService;
    @Inject
    private int remoteDebounceTimeout = 200;

    private final IntegerProperty pendingRequests = new SimpleIntegerProperty(0);

    private boolean subscriptionRunning;

    private String searchingFor;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        searchingFor = resources.getString("searchingFor");

        loader.managedProperty().bind(loader.visibleProperty());
        loader.visibleProperty().bind(pendingRequests.greaterThan(0));

        resultContainer.managedProperty().bind(resultContainer.visibleProperty());
        resultContainer.visibleProperty().bind(loader.visibleProperty().not());

        bindPhoneSearch();
        bindEmailSearch();

        Observable.<String>create(subscriber -> {
            search.textProperty().addListener((observable, oldValue, newValue) -> subscriber.onNext(newValue));
            subscriptionRunning = true;
        })
        .subscribeOn(Schedulers.computation())
        .map(this::formatInput)
        .filter(this::isValidInput)
        .debounce(remoteDebounceTimeout, TimeUnit.MILLISECONDS)
        .observeOn(Schedulers.io())
        .map(this::search)
        .filter(list -> !list.isEmpty())
        .observeOn(Schedulers.computation())
        .subscribe(contacts -> {
            Platform.runLater(resultContainer.getChildren()::clear);
            contacts.forEach(this::showContact);
        });
    }

    protected void bindPhoneSearch() {
        StringBinding formattedPhoneBinding = Bindings.createStringBinding(this::formatPhone, search.textProperty());
        phoneSearch.textProperty().bind(new SimpleStringProperty(searchingFor).concat(" ").concat(formattedPhoneBinding));
        BooleanBinding validPhoneBinding = Bindings.createBooleanBinding(this::isValidPhone, search.textProperty());
        phoneSearch.visibleProperty().bind(validPhoneBinding);
        phoneSearch.managedProperty().bind(phoneSearch.visibleProperty());
    }

    protected void bindEmailSearch() {
        emailSearch.textProperty().bind(new SimpleStringProperty(searchingFor).concat(" ").concat(search.textProperty()));
        BooleanBinding validEmailBinding = Bindings.createBooleanBinding(this::isValidEmail, search.textProperty());
        emailSearch.visibleProperty().bind(validEmailBinding);
        emailSearch.managedProperty().bind(emailSearch.visibleProperty());
    }

    private boolean isValidPhone() {
        return isValidPhoneNumber(search.getText());
    }

    private boolean isValidEmail() {
        return isValidEmail(search.getText());
    }

    @NotNull
    private String formatInput(String text) {
        return text.contains("@") ? text : formatPhone(text);
    }

    private String formatPhone() {
        return formatPhone(search.getText());
    }

    private String formatPhone(String unformattedNumber) {
        try {
            return formatPhoneNumber(unformattedNumber);
        } catch (NumberParseException e) {
            return "";
        }
    }

    boolean isSubscriptionRunning() {
        return subscriptionRunning;
    }

    private boolean isValidInput(String text) {
        return !text.isEmpty() && (isValidPhoneNumber(text) || isValidEmail(text));
    }

    private boolean isValidEmail(String text) {
        return text.length() > 3 && text.contains("@");
    }

    private void showContact(Contact contact) {
        new IndexSearchResultItemView(contact).place(resultContainer);
    }

    private void incrementPendingRequests() {
        synchronized (pendingRequests) {
            pendingRequests.set(pendingRequests.get()+1);
        }
    }

    private void decrementPendingRequests() {
        synchronized (pendingRequests) {
            pendingRequests.set(pendingRequests.get()-1);
        }
    }

    private List<Contact> search(String searchText) {
        incrementPendingRequests();
        try {
            if (isValidPhoneNumber(searchText)) {
                return indexService.searchContacts("", searchText);
            }
            return indexService.searchContacts(searchText, "");
        } catch (Exception e) {
            alert(e);
        } finally {
            decrementPendingRequests();
        }
        return Collections.emptyList();
    }
}
