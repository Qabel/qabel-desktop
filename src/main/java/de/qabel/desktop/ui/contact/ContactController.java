package de.qabel.desktop.ui.contact;

import com.google.gson.GsonBuilder;
import de.qabel.core.config.*;
import de.qabel.core.exceptions.QblDropInvalidURL;
import de.qabel.desktop.config.ClientConfiguration;
import de.qabel.desktop.repository.ContactRepository;
import de.qabel.desktop.repository.IdentityRepository;
import de.qabel.desktop.repository.exception.EntityNotFoundExcepion;
import de.qabel.desktop.repository.exception.PersistenceException;
import de.qabel.desktop.ui.AbstractController;
import de.qabel.desktop.ui.DetailsController;
import de.qabel.desktop.ui.DetailsView;
import de.qabel.desktop.ui.accounting.item.SelectionEvent;
import de.qabel.desktop.ui.actionlog.ActionlogController;
import de.qabel.desktop.ui.actionlog.ActionlogView;
import de.qabel.desktop.ui.contact.item.BlankItemView;
import de.qabel.desktop.ui.contact.item.ContactItemController;
import de.qabel.desktop.ui.contact.item.ContactItemView;
import de.qabel.desktop.ui.remotefs.RemoteFileDetailsController;
import de.qabel.desktop.ui.remotefs.RemoteFileDetailsView;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ScrollPane;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;

import javax.inject.Inject;
import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.*;

public class ContactController extends AbstractController implements Initializable {

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

	@Inject
	private ClientConfiguration clientConfiguration;

	@Inject
	private ContactRepository contactRepository;

	@Inject
	private IdentityRepository identityRepository;

	private List<ContactItemController> contactItems = new LinkedList<>();

	ActionlogController actionlogController;

	@FXML
	StackPane contactroot;

	DetailsController details;

	@Override
	public void initialize(URL location, ResourceBundle resources) {
		this.resourceBundle = resources;
		createObserver();
		try {
			buildGson();
			loadContacts();
		} catch (EntityNotFoundExcepion | PersistenceException e) {
			alert(e);
		}
		createButtonGraphics();

		DetailsView detailsView = new DetailsView();
		details = (DetailsController) detailsView.getPresenter();
		detailsView.getViewAsync(contactroot.getChildren()::add);
	}

	private void createButtonGraphics() {
		Image importGraphic = new Image(getClass().getResourceAsStream("/img/import.png"));
		importButton.setGraphic(new ImageView(importGraphic));

		Image exportGraphic = new Image(getClass().getResourceAsStream("/img/export.png"));
		exportButton.setGraphic(new ImageView(exportGraphic));
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
	protected void handleImportContactsButtonAction(ActionEvent event) throws IOException, PersistenceException, URISyntaxException, QblDropInvalidURL, EntityNotFoundExcepion {
		FileChooser chooser = new FileChooser();
		chooser.setTitle(resourceBundle.getString("contactDownloadFolder"));
		File file = chooser.showOpenDialog(contactList.getScene().getWindow());
		importContacts(file);
		loadContacts();
	}

	@FXML
	protected void handleExportContactsButtonAction(ActionEvent event) throws EntityNotFoundExcepion, IOException {
		FileChooser chooser = new FileChooser();
		chooser.setTitle(resourceBundle.getString("contactDownload"));
		chooser.setInitialFileName("Contacts.json");
		File file = chooser.showSaveDialog(contactList.getScene().getWindow());
		exportContacts(file);
	}

	void loadContacts() throws EntityNotFoundExcepion {
		contactList.getChildren().clear();
		contactItems.clear();

		i = clientConfiguration.getSelectedIdentity();

		String old = null;
		Contacts contacts = contactRepository.findContactsFromOneIdentity(i);
		List<Contact> cl = new LinkedList<>(contacts.getContacts());

		cl.sort((c1, c2) -> c1.getAlias().toLowerCase().compareTo(c2.getAlias().toLowerCase()));

		for (Contact co : cl) {
			if (old == null || !old.equals(co.getAlias().substring(0, 1).toUpperCase())) {
				old = createBlankItem(co);
			}
			createContactItem(co);
		}
	}

	private void createContactItem(Contact co) {
		final Map<String, Object> injectionContext = new HashMap<>();
		injectionContext.put("contact", co);
		ContactItemView itemView = new ContactItemView(injectionContext::get);
		ContactItemController controller = (ContactItemController) itemView.getPresenter();
		controller.addSelectionListener((selectionEvent) -> {
			unselectAll();
			select(selectionEvent);

		});
		contactList.getChildren().add(itemView.getView());
		contactItems.add(controller);
		itemViews.add(itemView);

	}

	private void select(SelectionEvent selectionEvent) {
		selectionEvent.getController().select();
		showActionlog(selectionEvent.getContact());
	}


	private void unselectAll() {
		for (ContactItemController c : contactItems) {
			c.unselect();
		}
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
		clientConfiguration.addObserver((o, arg) -> {
			if (!(arg instanceof Identity)) {
				return;
			}
			try {
				loadContacts();
			} catch (EntityNotFoundExcepion entityNotFoundExcepion) {
				entityNotFoundExcepion.printStackTrace();
			}
		});
	}

	void exportContacts(File file) throws EntityNotFoundExcepion, IOException {
		Contacts contacts = contactRepository.findContactsFromOneIdentity(i);
		String jsonContacts = gson.toJson(contacts);
		writeStringInFile(jsonContacts, file);
	}

	void importContacts(File file) throws IOException, URISyntaxException, QblDropInvalidURL, PersistenceException {
		String content = readFile(file);
		Identity i = clientConfiguration.getSelectedIdentity();
		try {
			Contacts contacts = gson.fromJson(content, Contacts.class);
			for (Contact c : contacts.getContacts()) {
				contactRepository.save(c, i);
			}
		} catch (Exception e){
			Contact contact = gson.fromJson(content, Contact.class);
			contactRepository.save(contact, i);
		}
	}

	protected void buildGson() throws EntityNotFoundExcepion, PersistenceException {
		final GsonBuilder builder = new GsonBuilder();
		builder.serializeNulls();
		Identities ids = identityRepository.findAll();
		builder.registerTypeAdapter(Contacts.class, new ContactsTypeAdapter(ids));
		builder.registerTypeAdapter(Contact.class, new ContactTypeAdapter());
		gson = builder.create();
	}
}




