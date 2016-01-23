package de.qabel.desktop.ui.actionlog;

import de.qabel.core.config.Contact;
import de.qabel.core.config.Identity;
import de.qabel.desktop.config.ClientConfiguration;
import de.qabel.desktop.repository.exception.EntityNotFoundExcepion;
import de.qabel.desktop.ui.AbstractController;
import de.qabel.desktop.ui.actionlog.item.ActionlogItemView;
import de.qabel.desktop.ui.actionlog.item.MyActionlogItemView;
import de.qabel.desktop.ui.actionlog.item.OtherActionlogItemView;
import de.qabel.desktop.ui.actionlog.item.OtherTextWrapper;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.VBox;

import javax.inject.Inject;
import java.net.URL;
import java.util.*;


public class ActionlogController extends AbstractController implements Initializable {


	ResourceBundle resourceBundle;
	List<ActionlogItemView> messageView = new LinkedList<>();


	@FXML
	VBox messages;

	@FXML
	ScrollPane scroller;

	@Inject
	ClientConfiguration clientConfiguration;


	public void initialize(URL location, ResourceBundle resources) {
		try {
			loadMessages();
		} catch (EntityNotFoundExcepion entityNotFoundExcepion) {
			entityNotFoundExcepion.printStackTrace();
		}
	}

	void loadMessages() throws EntityNotFoundExcepion {
		messages.getChildren().clear();
		createMessageItems();
		if(scroller.getVmax() == scroller.getVvalue()) {
			scroller.setVvalue(scroller.getVmax());
		}
	}

	private void createMessageItems() {
		String text;
		Map<String, Object> injectionContext = new HashMap<>();
		text = "Lorem ipsum dolor sit amet, consetetur sadipscing elitr, sed diam nonumy eirmod tempor invidunt ut labore et dolore magna aliquyam erat, sed diam voluptua. At vero eos et accusam et justo duo dolores et ea rebum. Stet clita kasd gubergren, no sea takimata sanctus est Lorem ipsum dolor sit amet. Lorem ipsum dolor sit amet, consetetur sadipscing elitr, sed diam nonumy eirmod tempor invidunt ut labore et dolore magna aliquyam erat, sed diam voluptua. At vero eos et accusam et justo duo dolores et ea rebum. Stet clita kasd gubergren, no sea takimata sanctus est Lorem ipsum dolor sit amet.</html>";
		injectionContext.put("text", text);
		MyActionlogItemView myItemView = new MyActionlogItemView(injectionContext::get);
		messages.getChildren().add(myItemView.getView());
		messageView.add(myItemView);

		injectionContext = new HashMap<>();

		OtherTextWrapper wrapper = new OtherTextWrapper();
		wrapper.setText(text);
		Identity i = clientConfiguration.getSelectedIdentity();
		Contact contact = new Contact(i, i.getAlias(), i.getDropUrls(), i.getEcPublicKey());
		wrapper.setContact(contact);
		injectionContext.put("wrapper", wrapper);
		OtherActionlogItemView otherItemView = new OtherActionlogItemView(injectionContext::get);
		messages.getChildren().add(otherItemView.getView());
		messageView.add(otherItemView);
		text = "short Text";
		injectionContext = new HashMap<>();
		injectionContext.put("text", text);
		myItemView = new MyActionlogItemView(injectionContext::get);
		messages.getChildren().add(myItemView.getView());
		messageView.add(myItemView);

		wrapper = new OtherTextWrapper();
		wrapper.setText(text);
		i = clientConfiguration.getSelectedIdentity();
		contact = new Contact(i, i.getAlias(), i.getDropUrls(), i.getEcPublicKey());
		wrapper.setContact(contact);
		injectionContext.put("wrapper", wrapper);
		otherItemView = new OtherActionlogItemView(injectionContext::get);
		messages.getChildren().add(otherItemView.getView());
		messageView.add(otherItemView);

	}


}
