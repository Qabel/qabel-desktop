package de.qabel.desktop.ui.contact;

import de.qabel.core.config.Contact;
import de.qabel.core.config.Identity;
import de.qabel.desktop.config.ClientConfiguration;
import de.qabel.desktop.config.factory.IdentityBuilderFactory;
import de.qabel.desktop.repository.IdentityRepository;
import de.qabel.desktop.ui.AbstractController;
import de.qabel.desktop.ui.contact.item.BlankItemView;
import de.qabel.desktop.ui.contact.item.ContactItemView;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.layout.Pane;

import javax.inject.Inject;
import java.net.URL;
import java.util.*;

public class ContactController extends AbstractController implements Initializable {

	ResourceBundle resourceBundle;
	List<ContactItemView> itemViews = new LinkedList<>();

	@FXML
	Pane contactList;

	@Inject
	private IdentityRepository identityRepository;

	@Inject
	private IdentityBuilderFactory identityBuilderFactory;

	@Inject
	private ClientConfiguration clientConfiguration;

	@Override
	public void initialize(URL location, ResourceBundle resources) {
		this.resourceBundle = resources;
		loadContacts();
	}

	@FXML
	protected void handleImportContactsButtonAction(ActionEvent event) {
	}

	@FXML
	protected void handleExportContactsButtonAction(ActionEvent event) {
	}

	private void loadContacts() {
		contactList.getChildren().clear();
		String old = null;
		List<Contact> contacts = new ArrayList<>();

		for (int j = 1; j >= 0; j--) {
			Identity i = identityBuilderFactory.factory().withAlias("Qabel").build();
			Contact c = new Contact(i, i.getAlias(), i.getDropUrls(), i.getEcPublicKey());
			c.setEmail("mail.awesome@qabel.de");
			contacts.add(c);

		}

		for (int j = 1; j >= 0; j--) {
			Identity i = identityBuilderFactory.factory().withAlias("Mesa").build();
			Contact c = new Contact(i, i.getAlias(), i.getDropUrls(), i.getEcPublicKey());
			c.setEmail("mesa@mesa-labs.de");
			contacts.add(c);

		}

		for (int j = 1; j >= 0; j--) {
			Identity i = identityBuilderFactory.factory().withAlias("prae").build();
			Contact c = new Contact(i, i.getAlias(), i.getDropUrls(), i.getEcPublicKey());
			c.setEmail("mail.awesome@prae.me");
			contacts.add(c);

		}

		contacts.sort((c1, c2) -> c1.getAlias().compareTo(c2.getAlias()));

		for (Contact co : contacts) {
			if ( old == null || !old.equals(co.getAlias().substring(0, 1).toUpperCase())) {
				old = createBlankItem(co);
			}
			createContactItem(co);
		}
	}

	private void createContactItem(Contact co) {
		final Map<String, Object> injectionContext = new HashMap<>();
		injectionContext.put("contact", co);
		ContactItemView itemView = new ContactItemView(injectionContext::get);
		contactList.getChildren().add(itemView.getView());
		itemViews.add(itemView);
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
}



