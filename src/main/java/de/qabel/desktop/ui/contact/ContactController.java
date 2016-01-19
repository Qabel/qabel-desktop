package de.qabel.desktop.ui.contact;

import de.qabel.core.config.Contact;
import de.qabel.core.config.Identity;
import de.qabel.desktop.config.ClientConfiguration;
import de.qabel.desktop.config.factory.IdentityBuilderFactory;
import de.qabel.desktop.repository.ContactRepository;
import de.qabel.desktop.repository.IdentityRepository;
import de.qabel.desktop.repository.exception.EntityNotFoundExcepion;
import de.qabel.desktop.repository.exception.PersistenceException;
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
	private ClientConfiguration clientConfiguration;

	@Inject
	private ContactRepository contactRepository;

	@Override
	public void initialize(URL location, ResourceBundle resources) {
		this.resourceBundle = resources;
		try {
			Identity i = clientConfiguration.getSelectedIdentity();
			setup(i);
			loadContacts(i);
		} catch (EntityNotFoundExcepion | PersistenceException entityNotFoundExcepion) {
			entityNotFoundExcepion.printStackTrace();
		}
	}

	@FXML
	protected void handleImportContactsButtonAction(ActionEvent event) {
	}

	@FXML
	protected void handleExportContactsButtonAction(ActionEvent event) {
	}

	void loadContacts(Identity i) throws EntityNotFoundExcepion {
		contactList.getChildren().clear();
		String old = null;
		List<Contact> contacts = contactRepository.findAllContactFormOneIdentity(i);

		contacts.sort((c1, c2) -> c1.getAlias().compareTo(c2.getAlias()));

		for (Contact co : contacts) {
			if (old == null || !old.equals(co.getAlias().substring(0, 1).toUpperCase())) {
				old = createBlankItem(co);
			}
			createContactItem(co);
		}
	}

	private void setup(Identity i) throws PersistenceException {
		Contact c;
		c = new Contact(i, i.getAlias(), i.getDropUrls(), i.getEcPublicKey());
		c.setAlias("Qabel");
		c.setEmail("mail.awesome@qabel.de");
		contactRepository.save(c);


		c = new Contact(i, i.getAlias(), i.getDropUrls(), i.getEcPublicKey());
		c.setAlias("MESA");
		c.setEmail("mesa@mesa-labs.de");
		contactRepository.save(c);


		c = new Contact(i, i.getAlias(), i.getDropUrls(), i.getEcPublicKey());
		c.setAlias("preamandatum");
		c.setEmail("mail.awesome@prae.me");
		contactRepository.save(c);
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



