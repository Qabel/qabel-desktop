package de.qabel.desktop.ui.invite;

import de.qabel.desktop.ui.AbstractController;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.TextArea;

import java.awt.*;
import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.util.ResourceBundle;


public class InviteController extends AbstractController implements Initializable {


	@FXML
	private TextArea textarea;

	public void initialize(URL location, ResourceBundle resources) {
		textarea.setText(resources.getString("inviteText"));
		textarea.setEditable(false);
	}

	@FXML
	protected void handleInviteButtonAction(ActionEvent event) throws IOException {
		Desktop desktop;
		if (Desktop.isDesktopSupported()
				&& (desktop = Desktop.getDesktop()).isSupported(Desktop.Action.MAIL)) {
			String message = "mailTo:markus.thimm@gmail.com" + "?subject=" + "TEST%20SUBJECT"
					+ "&body=" + "TEST%20BODY";
			URI uri = URI.create(message);
			desktop.browse(uri);
		}

	}


}