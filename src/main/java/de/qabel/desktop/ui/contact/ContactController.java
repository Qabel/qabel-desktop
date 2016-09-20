package de.qabel.desktop.ui.contact;

import com.airhacks.afterburner.views.QabelFXMLView;
import com.google.gson.GsonBuilder;
import de.qabel.core.config.*;
import de.qabel.core.exceptions.QblDropInvalidURL;
import de.qabel.core.repository.ContactRepository;
import de.qabel.core.repository.IdentityRepository;
import de.qabel.core.repository.TransactionManager;
import de.qabel.core.repository.exception.EntityNotFoundException;
import de.qabel.core.repository.exception.PersistenceException;
import de.qabel.desktop.config.ClientConfig;
import de.qabel.desktop.ui.AbstractController;
import de.qabel.desktop.ui.DetailsController;
import de.qabel.desktop.ui.DetailsView;
import de.qabel.desktop.ui.accounting.item.SelectionEvent;
import de.qabel.desktop.ui.actionlog.ActionlogController;
import de.qabel.desktop.ui.actionlog.ActionlogView;
import de.qabel.desktop.ui.contact.context.AssignContactView;
import de.qabel.desktop.ui.contact.item.BlankItemView;
import de.qabel.desktop.ui.contact.item.ContactItemController;
import de.qabel.desktop.ui.contact.item.ContactItemView;
import de.qabel.desktop.ui.contact.item.DummyItemView;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import org.controlsfx.control.PopOver;
import org.json.JSONException;

import javax.inject.Inject;
import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.*;
import java.util.stream.Collectors;

public class ContactController extends AbstractController implements Initializable, EntityObserver {

    ResourceBundle resourceBundle;
    List<ContactItemView> itemViews = new LinkedList<>();
    Identity i;

    @FXML
    Pane contactList;

    @FXML
    VBox actionlogViewPane;

    @FXML
    Button importButton;

    @FXML
    Button exportButton;

    @FXML
    VBox contacts;

    @FXML
    ComboBox<Label> filterCombo;

    @Inject
    private ClientConfig clientConfiguration;

    @Inject
    private ContactRepository contactRepository;

    @Inject
    private IdentityRepository identityRepository;

    @Inject
    private TransactionManager transactionManager;

    List<ContactItemController> contactItems = new LinkedList<>();

    ActionlogController actionlogController;

    @FXML
    StackPane contactroot;

    DetailsController details;
    Contacts contactsFromRepo;
    Label showNormalContacts;
    Label showIgnoredContacts;
    Label showNewContacts;

    enum ContactsFilter {
        ALL, NEW, IGNORED
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        resourceBundle = resources;

        i = clientConfiguration.getSelectedIdentity();

        createButtonGraphics();

        DetailsView detailsView = new DetailsView();
        details = (DetailsController) detailsView.getPresenter();
        detailsView.getViewAsync(contactroot.getChildren()::add);

        initFilterCombo(resources);

        try {
            buildGson();
            loadContacts();
            createObserver();

        } catch (EntityNotFoundException | PersistenceException e) {
            alert(e);
        }

    }

    private void initFilterCombo(ResourceBundle resources) {
        initFilterLabels(resources);
        filterCombo.getItems().addAll(showNormalContacts, showNewContacts, showIgnoredContacts);
        filterCombo.getSelectionModel().select(showNormalContacts);
        filterCombo.valueProperty().addListener((observable, oldValue, newValue) -> { update(); });
    }

    private void initFilterLabels(ResourceBundle resources) {
        showNormalContacts = new Label(resources.getString("showNormalContacts"));
        showNormalContacts.setId("showNormalContacts");
        showIgnoredContacts = new Label(resources.getString("showIgnoredContacts"));
        showIgnoredContacts.setId("showIgnoredContacts");
        showNewContacts = new Label(resources.getString("showNewContacts"));
        showNewContacts.setId("showNewContacts");
    }

    private void createButtonGraphics() {
        Image importGraphic = new Image(getClass().getResourceAsStream("/img/import_black.png"));
        importButton.setGraphic(new ImageView(importGraphic));
        Tooltip.install(importButton, new Tooltip(resourceBundle.getString("contactImport")));
        Image exportGraphic = new Image(getClass().getResourceAsStream("/img/export.png"));
        exportButton.setGraphic(new ImageView(exportGraphic));
        Tooltip.install(exportButton, new Tooltip(resourceBundle.getString("contactExport")));
    }

    private void showActionlog(Contact contact) {
        if (actionlogController == null) {
            ActionlogView actionlogView = new ActionlogView();
            actionlogController = (ActionlogController) actionlogView.getPresenter();
            actionlogView.getViewAsync(details::show);
        } else {
            details.show();
        }
        actionlogController.setContact(contact);
    }

    @FXML
    protected void handleImportContactsButtonAction(ActionEvent event) throws IOException, PersistenceException, URISyntaxException, QblDropInvalidURL, EntityNotFoundException, JSONException {
        FileChooser chooser = new FileChooser();
        chooser.setTitle(resourceBundle.getString("contactDownloadFolder"));
        FileChooser.ExtensionFilter qcoExtensionFilter = new FileChooser.ExtensionFilter(resourceBundle.getString("qcoExtensionFilterLabel"), "*.qco");
        chooser.getExtensionFilters().add(qcoExtensionFilter);
        File file = chooser.showOpenDialog(contactList.getScene().getWindow());
        try {
            importContacts(file);
            loadContacts();
        } catch (IOException | PersistenceException | JSONException e) {
            alert(resourceBundle.getString("alertImportContactFail"), e);
        } catch (NullPointerException ignored) {
        }
    }

    @FXML
    protected void handleExportContactsButtonAction() throws EntityNotFoundException, IOException, JSONException {
        tryOrAlert(() -> {
            try {
                FileChooser chooser = new FileChooser();
                chooser.setTitle(resourceBundle.getString("contactDownload"));
                chooser.setInitialFileName("Contacts.qco");
                File file = chooser.showSaveDialog(contactList.getScene().getWindow());

                exportContacts(file);
            } catch (NullPointerException ignored) {
            }
        });
    }

    List<Contact> filteredContacts(Collection<Contact> contacts, ContactsFilter filter) {
        return contacts.stream().filter(contact -> {
            switch (filter) {
                case ALL:
                    return !contact.isIgnored() && (contact.getStatus() == Contact.ContactStatus.NORMAL
                        || contact.getStatus() == Contact.ContactStatus.UNKNOWN);
                case NEW:
                    return !contact.isIgnored() && contact.getStatus() == Contact.ContactStatus.UNKNOWN;
                case IGNORED:
                    return contact.isIgnored();
            }
            return  false;
        }).collect(Collectors.toList());
    }


    public void loadContacts() {
        contactList.getChildren().clear();
        contactItems.clear();

        i = clientConfiguration.getSelectedIdentity();
        if (i == null) {
            return;
        }

        String old = null;
        try {
            contactsFromRepo = contactRepository.find(i);
        } catch (PersistenceException e) {
            alert("failed to load contacts", e);
            return;
        }
        if (contactsFromRepo.getContacts().isEmpty()) {
            final Map<String, Object> injectionContext = new HashMap<>();
            DummyItemView itemView = new DummyItemView(injectionContext::get);
            contactList.getChildren().add(itemView.getView());
            return;
        }
        Label selectedItem = filterCombo.getSelectionModel().getSelectedItem();
        ContactsFilter filter;
        if (selectedItem == showNewContacts) {
            filter = ContactsFilter.NEW;
        } else if (selectedItem == showIgnoredContacts) {
            filter = ContactsFilter.IGNORED;
        } else {
            filter = ContactsFilter.ALL;
        }
        List<Contact> cl = filteredContacts(contactsFromRepo.getContacts(), filter);

        cl.sort((c1, c2) -> c1.getAlias().toLowerCase().compareTo(c2.getAlias().toLowerCase()));

        for (Contact co : cl) {
            if (old == null || !old.equals(co.getAlias().substring(0, 1).toUpperCase())) {
                old = createBlankItem(co);
            }
            createContactItem(co);
        }
    }

    void createContactItem(Contact co) {
        final Map<String, Object> injectionContext = new HashMap<>();
        injectionContext.put("contact", co);
        ContactItemView itemView = new ContactItemView(injectionContext::get);
        ContactItemController controller = (ContactItemController) itemView.getPresenter();
        controller.addSelectionListener(selectionEvent -> {
            unselectAll();
            select(selectionEvent);
        });
        controller.addContextListener(this::showAssignContactPopover);
        contactList.getChildren().add(itemView.getView());
        contactItems.add(controller);
        itemViews.add(itemView);

    }

    private void select(SelectionEvent selectionEvent) {
        selectionEvent.getController().select();
        showActionlog(selectionEvent.getContact());
    }

    private PopOver assignContactPopover;

    private void showAssignContactPopover(SelectionEvent selectionEvent) {
        Contact contact = selectionEvent.getContact();
        ContactItemController controller = selectionEvent.getController();
        new AssignContactView(contact).getViewAsync(view -> {
            if (assignContactPopover != null) {
                assignContactPopover.hide();
            }
            assignContactPopover = new PopOver(view);
            assignContactPopover.getStyleClass().add("assignContactPopover");

            assignContactPopover.setTitle(contact.getAlias());
            assignContactPopover.setHeaderAlwaysVisible(true);
            assignContactPopover.getRoot().getStyleClass().add("assignContactPopover");
            assignContactPopover.getRoot().getStylesheets().add(QabelFXMLView.getGlobalStyleCheat());

            assignContactPopover.show(controller.getContactRootItem(), selectionEvent.getScreenX(), selectionEvent.getScreenY());
        });
    }

    private void unselectAll() {
        contactItems.forEach(ContactItemController::unselect);
        details.hide();
    }

    private String createBlankItem(Contact co) {
        String old = co.getAlias().substring(0, 1).toUpperCase();
        final Map<String, Object> injectionContext = new HashMap<>();
        injectionContext.put("contact", co);
        BlankItemView itemView = new BlankItemView(injectionContext::get);
        contactList.getChildren().add(itemView.getView());
        itemViews.add(itemView);
        return old;
    }

    private void createObserver() {
        contactsFromRepo.addObserver(this);
        clientConfiguration.onSelectIdentity(i -> {
            contactsFromRepo.removeObserver(this);
            loadContacts();
            contactsFromRepo.addObserver(this);
        });
    }

    void exportContacts(File file) throws EntityNotFoundException, IOException, JSONException, PersistenceException {
        Contacts contacts = contactRepository.find(i);
        String jsonContacts = ContactExportImport.exportContacts(contacts);
        writeStringInFile(jsonContacts, file);
    }

    void importContacts(File file) throws IOException, URISyntaxException, QblDropInvalidURL, PersistenceException, JSONException {
        String content = readFile(file);
        i = clientConfiguration.getSelectedIdentity();
        transactionManager.transactional(() -> {
            try {
                Contacts contacts = ContactExportImport.parseContactsForIdentity(i, content);
                for (Contact c : contacts.getContacts()) {
                    contactRepository.save(c, i);
                }
            } catch (Exception ignore) {
                Contact c = ContactExportImport.parseContactForIdentity(content);
                contactRepository.save(c, i);
            }
        });
    }

    private void buildGson() throws EntityNotFoundException, PersistenceException {
        final GsonBuilder builder = new GsonBuilder();
        builder.serializeNulls();
        Identities ids = identityRepository.findAll();
        builder.registerTypeAdapter(Contacts.class, new ContactsTypeAdapter(ids));
        builder.registerTypeAdapter(Contact.class, new ContactTypeAdapter());
        gson = builder.create();
    }

    @Override
    public void update() {
        Platform.runLater(this::loadContacts);
    }
}




