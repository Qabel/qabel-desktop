package de.qabel.desktop.ui.accounting.interactor;

import de.qabel.core.config.Identity;
import de.qabel.desktop.ui.util.FileChooserFactory;
import javafx.stage.Window;
import org.jetbrains.annotations.NotNull;
import rx.Single;

import javax.inject.Inject;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.concurrent.Callable;

public abstract class AbstractExportIdentityInteractor {
    @Inject
    protected ResourceBundle resourceBundle;
    @Inject
    protected FileChooserFactory fileChooserFactory;

    public AbstractExportIdentityInteractor(FileChooserFactory fileChooserFactory, ResourceBundle resourceBundle) {
        this.fileChooserFactory = fileChooserFactory;
        this.resourceBundle = resourceBundle;
    }

    public Single<Boolean> export(Identity identity, Window window) {
        String title = getTitle();
        String filterName = getExtensionFilterName();
        String defaultName = identity.getAlias() + getExtension();
        return export(
            identity,
            () -> fileChooserFactory.create(title, defaultName, filterName, getExtension()).apply(window)
        );
    }

    @NotNull
    protected abstract String getExtensionFilterName();

    @NotNull
    protected abstract String getTitle();

    @NotNull
    protected abstract String getExtension();

    private Single<Boolean> export(Identity identity, Callable<Optional<File>> fileChooser) {
        return Single.fromCallable(() -> {
            Optional<File> file = fileChooser.call();
            if (!file.isPresent()) {
                return false;
            }

            export(identity, file.get());
            return true;
        });
    }

    private void export(Identity identity, File target) throws IOException {
        String json = convertIdentity(identity);
        writeStringInFile(json, target);
    }

    protected abstract String convertIdentity(Identity identity);

    private void writeStringInFile(String json, File file) throws IOException {
        File targetFile = new File(file.getPath());
        targetFile.createNewFile();
        OutputStream outStream = new FileOutputStream(targetFile);
        outStream.write(json.getBytes());
    }
}
