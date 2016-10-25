package de.qabel.desktop.ui.util;

import javafx.stage.Window;

import java.io.File;
import java.util.Optional;
import java.util.concurrent.Callable;
import java.util.function.Function;

public class CallbackFileChooserFactory implements FileChooserFactory {
    private Callable<File> callable;

    public CallbackFileChooserFactory(Callable<File> callable) {
        this.callable = callable;
    }

    public String lastTitle;
    public String lastDefaultFileName;
    public String lastFilterName;
    public String lastFilterExtension;

    @Override
    public Function<Window, Optional<File>> create(String title, String defaultFileName, String filterName, String filterExtension) {
        lastTitle = title;
        lastDefaultFileName = defaultFileName;
        lastFilterName = filterName;
        lastFilterExtension = filterExtension;

        return window -> {
            try {
                return Optional.ofNullable(callable.call());
            } catch (Exception e) {
                throw new IllegalStateException(e);
            }
        };
    }
}
