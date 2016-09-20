package de.qabel.desktop.ui.contact;

import com.airhacks.afterburner.views.QabelFXMLView;
import com.google.gson.GsonBuilder;
import de.qabel.core.config.*;
import de.qabel.core.repository.ContactRepository;
import de.qabel.core.repository.IdentityRepository;
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
import de.qabel.desktop.ui.contact.menu.ContactMenuController;
import de.qabel.desktop.ui.contact.menu.ContactMenuView;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import org.controlsfx.control.PopOver;
import org.json.JSONException;

import javax.inject.Inject;

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
    Button searchButton;

    @FXML
    Button contactsButton;

    @FXML
    Button contactMenu;

    @FXML
    ComboBox<Label> filterCombo;

    @Inject
    private ClientConfig clientConfiguration;

    @Inject
    private ContactRepository contactRepository;

    @Inject
    private IdentityRepository identityRepository;

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


    public ContactMenuController contactMenuController;
    ContactMenuView contactMenuView;

    public PopOver popOver;

    private static ImageView searchButtonImageView = setImageView(loadImage("/icon/search.png"));
    private static ImageView menuImageView = setImageView(loadImage("/img/dots_vertical.png"));

    private static Image loadImage(String resourcePath) {
        return new Image(ContactMenuController.class.getResourceAsStream(resourcePath), 25, 25, true, true);
    }

    private static ImageView setImageView(Image image) {
        return new ImageView(image);
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        resourceBundle = resources;

        i = clientConfiguration.getSelectedIdentity();

        createButtonGraphics();

        DetailsView detailsView = new DetailsView();
        details = (DetailsController) detailsView.getPresenter();
        detailsView.getViewAsync(contactroot.getChildren()::add);

        contactRepository.attach(this::update);
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
        searchButton.setGraphic(searchButtonImageView);
        contactMenu.setGraphic(menuImageView);
        Tooltip.install(searchButton, new Tooltip(resourceBundle.getString("searchContact")));
        Tooltip.install(contactMenu, new Tooltip(resourceBundle.getString("contactsMenu")));
    }

    private void showActionlog(Contact contact) {
        if (popOver != null) {
            popOver.hide();
        }

        if (actionlogController == null) {
            ActionlogView actionlogView = new ActionlogView();
            actionlogController = (ActionlogController) actionlogView.getPresenter();
            actionlogView.getViewAsync(details::show);
        } else {
            details.show();
        }
        actionlogController.setContact(contact);
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
            return false;
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

    public void openContactMenu(MouseEvent event) {
        initializeMenu(event.getScreenX(), event.getScreenY());
        Platform.runLater(() -> contactMenuController.open());
    }

    private void initializeMenu(double coordX, double coordY) {
        contactMenuView = new ContactMenuView();
        contactMenuView.getView(view -> {
            popOver = new PopOver();
            popOver.setArrowLocation(PopOver.ArrowLocation.TOP_RIGHT);
            popOver.setContentNode(new VBox(contactMenuController.menuContact));
            popOver.show(contactList, coordX, coordY);
        });

        contactMenuController = (ContactMenuController) contactMenuView.getPresenter();
    }
}

