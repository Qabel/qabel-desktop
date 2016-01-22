package de.qabel.desktop.ui.actionlog;

import de.qabel.core.config.Account;
import de.qabel.core.config.Identity;
import de.qabel.core.crypto.QblECKeyPair;
import de.qabel.desktop.config.ClientConfiguration;
import de.qabel.desktop.repository.exception.EntityNotFoundExcepion;
import de.qabel.desktop.ui.AbstractController;
import de.qabel.desktop.ui.AbstractControllerTest;
import de.qabel.desktop.ui.actionlog.item.ActionlogItemView;
import de.qabel.desktop.ui.actionlog.item.MyActionlogItemView;
import de.qabel.desktop.ui.actionlog.item.OtherActionlogItemView;
import de.qabel.desktop.ui.contact.ContactController;
import de.qabel.desktop.ui.contact.ContactView;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.layout.VBox;
import org.junit.Test;

import javax.inject.Inject;
import java.net.URL;
import java.util.*;

import static org.junit.Assert.assertNotNull;


public class ActionlogControllerTest extends AbstractControllerTest {

	ActionlogController controller;

	@Test
	public void injectTest() {
		ActionlogView view = new ActionlogView();
		Identity i = new Identity("test", null, new QblECKeyPair());
		clientConfiguration.selectIdentity(i);
		clientConfiguration.setAccount(new Account("Provider", "user", "auth"));
		controller = (ActionlogController) view.getPresenter();
		assertNotNull(controller);
	}
}