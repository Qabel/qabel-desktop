package de.qabel.desktop.ui.util;

import javafx.stage.Window;

import java.io.File;
import java.util.Optional;
import java.util.function.Function;

public interface FileChooserFactory {
    Function<Window, Optional<File>> create(String title, String defaultFileName, String filterName, String filterExtension);
}
