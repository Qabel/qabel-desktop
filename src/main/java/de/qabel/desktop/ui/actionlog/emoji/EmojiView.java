package de.qabel.desktop.ui.actionlog.emoji;

import com.vdurmont.emoji.Emoji;
import javafx.collections.ObservableList;
import javafx.util.Callback;
import org.controlsfx.control.GridCell;
import org.controlsfx.control.GridView;
import rx.subjects.Subject;

public class EmojiView  extends GridView<Emoji> {
    public EmojiView(Subject<Emoji, Emoji> emojiSelect, ObservableList<Emoji> emojis) {
        Callback<GridView<Emoji>, GridCell<Emoji>> gridCellFactory = grid -> {
            EmojiGridCell emojiGridCell = new EmojiGridCell(emojiSelect);
            emojiGridCell.setPrefWidth(grid.getCellWidth());
            emojiGridCell.setPrefHeight(grid.getCellHeight());
            return emojiGridCell;
        };

        setCellWidth(25);
        setCellHeight(25);
        setHorizontalCellSpacing(5);
        setVerticalCellSpacing(5);

        setCellFactory(gridCellFactory);
        setItems(emojis);
    }
}
