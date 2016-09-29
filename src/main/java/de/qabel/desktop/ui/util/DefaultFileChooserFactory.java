package de.qabel.desktop.ui.util;

import javafx.stage.FileChooser;
import javafx.stage.Window;

import java.io.File;
import java.util.Optional;
import java.util.function.Function;

public class DefaultFileChooserFactory implements FileChooserFactory {
    @Override
    public Function<Window, Optional<File>> create(
        String title,
        String defaultFileName,
        String filterName,
        String filterExtension) {
        return window -> {
            FileChooser chooser = new FileChooser();
            chooser.setTitle(title);
            chooser.setInitialFileName(defaultFileName);
            return Optional.ofNullable(chooser.showSaveDialog(window));
        };
    }
}
