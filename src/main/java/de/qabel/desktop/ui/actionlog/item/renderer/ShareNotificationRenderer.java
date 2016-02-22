package de.qabel.desktop.ui.actionlog.item.renderer;

import de.qabel.desktop.SharingService;
import de.qabel.desktop.daemon.drop.ShareNotificationMessage;
import de.qabel.desktop.storage.AuthenticatedDownloader;
import de.qabel.desktop.storage.BoxObject;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.scene.text.TextAlignment;

public class ShareNotificationRenderer implements MessageRenderer {
	private AuthenticatedDownloader downloader;
	private SharingService sharingService;

	public ShareNotificationRenderer(AuthenticatedDownloader downloader, SharingService sharingService) {
		this.downloader = downloader;
		this.sharingService = sharingService;
	}

	@Override
	public Node render(String dropPayload) {
		VBox result = new VBox();
		result.getStyleClass().add("message-text");
		result.setStyle("-fx-spacing: 1em;");

		ShareNotificationMessage message = ShareNotificationMessage.fromJson(dropPayload);
		Label text = new Label(message.getMessage());
		result.getChildren().add(text);

		try {
			BoxObject file = sharingService.loadFileMetadata(message, downloader);
			Label label = new Label("Datei: " + file.getName());
			result.getChildren().add(label);
		} catch (Exception e) {
			result.getChildren().add(new Label("%failed_to_fetch_share_metadata"));
		}

		return result;
	}
}
