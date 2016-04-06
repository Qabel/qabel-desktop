package de.qabel.desktop.ui.actionlog.item.renderer;

import de.qabel.desktop.SharingService;
import de.qabel.desktop.daemon.drop.ShareNotificationMessage;
import de.qabel.desktop.exceptions.QblStorageNotFound;
import de.qabel.desktop.storage.AuthenticatedDownloader;
import de.qabel.desktop.storage.BoxObject;
import javafx.application.Platform;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.util.ResourceBundle;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ShareNotificationRenderer implements MessageRenderer {
    private static final ExecutorService renderExecutor = Executors.newSingleThreadExecutor();
    private AuthenticatedDownloader downloader;
    private SharingService sharingService;

    public ShareNotificationRenderer(AuthenticatedDownloader downloader, SharingService sharingService) {
        this.downloader = downloader;
        this.sharingService = sharingService;
    }

    @Override
    public Node render(String dropPayload, ResourceBundle resourceBundle) {
        VBox result = new VBox();
        result.getStyleClass().add("message-text");
        result.setStyle("-fx-spacing: 1em;");

        ShareNotificationMessage message = ShareNotificationMessage.fromJson(dropPayload);
        Label text = new Label(message.getMsg());
        result.getChildren().add(text);

            HBox fileBox = new HBox();
            fileBox.setSpacing(10.0);
            ImageView image = new ImageView(new Image(ShareNotificationRenderer.class.getResourceAsStream("/icon/share_inverse.png"), 16, 16, true, true));
            image.getStyleClass().add("payload-type-icon");
            fileBox.getChildren().add(image);

            Label label = new Label("...");
            fileBox.getChildren().add(label);
            result.getChildren().add(fileBox);

            renderExecutor.submit(() -> {
                try {
                    try {
                        BoxObject file = sharingService.loadFileMetadata(message, downloader);
                        Platform.runLater(() -> label.setText(file.getName()));
                    } catch (QblStorageNotFound e) {
                        Platform.runLater(() -> label.setText(resourceBundle.getString("sharedFileNoLongerAvailable")));
                    }
                } catch (Exception e) {
                    Platform.runLater(() -> result.getChildren().add(new Label("%remoteFileFailedToFetchShareMetadata")));
                }
            });

        return result;
    }
}
