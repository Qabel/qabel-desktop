package de.qabel.desktop.ui.actionlog.emoji;

import com.jfoenix.controls.JFXTextField;
import com.sun.javafx.collections.ObservableListWrapper;
import com.vdurmont.emoji.Emoji;
import com.vdurmont.emoji.EmojiManager;
import javafx.application.Platform;
import javafx.collections.transformation.FilteredList;
import javafx.scene.layout.BorderPane;
import rx.Observable;
import rx.subjects.PublishSubject;
import rx.subjects.Subject;

import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.function.Predicate;

public class EmojiSelector extends BorderPane {
    private Subject<Emoji, Emoji> emojiSelect = PublishSubject.create();
    private JFXTextField search = new JFXTextField();
    private List<Emoji> emojis = (List<Emoji>)EmojiManager.getAll();
    private FilteredList<Emoji> filteredEmojis = new FilteredList<>(new ObservableListWrapper<>(emojis));

    public EmojiSelector() {
        getStyleClass().add("emoji-selector");
        setPrefWidth(350);
        setPrefHeight(350);
        setMaxWidth(1000);
        setMaxHeight(1000);

        setTop(search);
        EmojiView selector = new EmojiView(emojiSelect, filteredEmojis);
        setCenter(selector);

        addFilter();
    }

    private void addFilter() {

        Observable.<String>create(subscriber ->
            search.textProperty().addListener((o, old, newValue) -> subscriber.onNext(newValue)))
            .debounce(250, TimeUnit.MILLISECONDS)
            .subscribe(filter -> {
                System.out.println("filtering by " + filter);
                String normalizedFilter = filter.toLowerCase();
                Predicate<String> aliasFilter = alias -> alias.contains(normalizedFilter);

                Platform.runLater(() ->
                    filteredEmojis.setPredicate(emoji -> emoji.getAliases().stream().filter(aliasFilter).count() > 0));
            });
    }

    public Observable<Emoji> onSelect() {
        return emojiSelect;
    }
}
