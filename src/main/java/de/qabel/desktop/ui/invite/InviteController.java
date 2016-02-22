package de.qabel.desktop.ui.invite;

import de.qabel.desktop.ui.AbstractController;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.TextArea;

import java.awt.*;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ResourceBundle;


public class InviteController extends AbstractController implements Initializable {


	@FXML
	private TextArea textarea;

	private ResourceBundle bundle;
	public void initialize(URL location, ResourceBundle resources) {

		this.bundle = resources;
		textarea.setText(bundle.getString("inviteText"));
		textarea.setEditable(false);
	}

	@FXML
	protected void handleInviteButtonAction(ActionEvent event) throws IOException {
		new Thread(() -> {
			Desktop desktop;
			if (Desktop.isDesktopSupported()
					&& (desktop = Desktop.getDesktop()).isSupported(Desktop.Action.MAIL)) {

				String subject = createEMailSubject();
				String body = createEMailBody();

				String mailURIStr = String.format("mailto:%s?subject=%s&cc=%s&body=%s",
						"", subject, "", body);

				try {
					desktop.mail( new URI(mailURIStr));
				} catch (URISyntaxException | IOException e) {
					e.printStackTrace();
				}
			}
		}
		).start();
	}

	String createEMailBody() {
		String body =  bundle.getString("inviteText");
		body = body.replace("\n", "%0D%0A");
		body = body.replace(" ", "%20");
		return body;
	}

	String createEMailSubject() {
		String subject = bundle.getString("inviteEmailSubjectText");
		subject = subject.replace(" ", "%20");
		return subject;
	}

}


