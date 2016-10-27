package de.qabel.desktop.ui.actionlog.emoji;

import com.vdurmont.emoji.Emoji;
import de.qabel.desktop.ui.util.Icons;
import javafx.scene.control.ContentDisplay;
import javafx.scene.image.ImageView;
import org.controlsfx.control.GridCell;
import rx.subjects.Subject;

public class EmojiGridCell extends GridCell<Emoji> {
    private Subject<Emoji, Emoji> emojiSelect;

    public EmojiGridCell(Subject<Emoji, Emoji> emojiSelect) {
        this.emojiSelect = emojiSelect;
        getStyleClass().add("emoji-cell");
        getStyleClass().add("clickable");
    }

    @Override
    protected void updateItem(Emoji emoji, boolean empty) {
        if (emoji == getItem()) {
            return;
        }
        super.updateItem(emoji, empty);
        if (!empty) {
            try {
                setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
                loadEmojiImage(emoji);
            } catch (Exception e) {
                setText(emoji.getUnicode());
                setContentDisplay(ContentDisplay.TEXT_ONLY);
            }
        } else {
            setText(null);
            setGraphic(null);
        }
    }

    private void loadEmojiImage(Emoji emoji) {
        String emojiCode = emoji.getHtmlHexadecimal().replace(";&#x", "_").replace(";", "").replace("&#x", "");
        String filename = "/icon/emoji/emoji_" + emojiCode + (emoji.supportsFitzpatrick() ? "_1f3fb" : "") + ".png";
        setOnMouseClicked(event -> emojiSelect.onNext(emoji));
        int width = getWidth() == 0 ? 25 : (int) getWidth();
        ImageView icon = Icons.getIcon(filename, width);
        icon.fitWidthProperty().bind(widthProperty());
        setGraphic(icon);
    }
}
