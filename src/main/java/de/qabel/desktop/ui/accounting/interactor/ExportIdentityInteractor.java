package de.qabel.desktop.ui.accounting.interactor;

import de.qabel.core.config.Identity;
import de.qabel.core.config.IdentityExportImport;
import javafx.stage.FileChooser;
import javafx.stage.Window;
import rx.Single;

import javax.inject.Inject;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.concurrent.Callable;

public class ExportIdentityInteractor {
    private static final String IDENTITY_EXTENSION = ".qid";

    @Inject
    private ResourceBundle resourceBundle;

    public ExportIdentityInteractor(ResourceBundle resourceBundle) {
        this.resourceBundle = resourceBundle;
    }

    public Single<Boolean> export(Identity identity, Window window) {
        return export(identity, () -> createSaveFileChooser(identity.getAlias() + IDENTITY_EXTENSION, window));
    }

    public Single<Boolean> export(Identity identity, Callable<Optional<File>> fileChooser) {
        return Single.fromCallable(() -> {
            Optional<File> file = fileChooser.call();
            if (!file.isPresent()) {
                return false;
            }

            export(identity, file.get());
            return true;
        });
    }

    private Optional<File> createSaveFileChooser(String defaultName, Window window) {
        FileChooser chooser = new FileChooser();
        chooser.setTitle(resourceBundle.getString("accountingExportIdentity"));
        chooser.setInitialFileName(defaultName);
        return Optional.ofNullable(chooser.showSaveDialog(window));
    }

    private void export(Identity identity, File target) throws IOException {
        String json = IdentityExportImport.exportIdentity(identity);
        writeStringInFile(json, target);
    }

    private void writeStringInFile(String json, File file) throws IOException {
        File targetFile = new File(file.getPath());
        targetFile.createNewFile();
        OutputStream outStream = new FileOutputStream(targetFile);
        outStream.write(json.getBytes());
    }
}
