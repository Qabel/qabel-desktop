package de.qabel.desktop.ui.accounting.interactor;

import de.qabel.core.config.Identity;
import de.qabel.core.config.IdentityExportImport;
import de.qabel.desktop.ui.util.FileChooserFactory;
import org.jetbrains.annotations.NotNull;

import javax.inject.Inject;
import java.util.ResourceBundle;

public class ExportIdentityInteractor extends AbstractExportIdentityInteractor {
    @Inject
    public ExportIdentityInteractor(ResourceBundle resourceBundle, FileChooserFactory fileChooserFactory) {
        super(fileChooserFactory, resourceBundle);
    }

    @Override
    @NotNull
    protected String getExtensionFilterName() {
        return resourceBundle.getString("qidExtensionFilterLabel");
    }

    @Override
    @NotNull
    protected String getTitle() {
        return resourceBundle.getString("accountingExportIdentity");
    }

    @Override
    @NotNull
    protected String getExtension() {
        return ".qid";
    }

    @Override
    protected String convertIdentity(Identity identity) {
        return IdentityExportImport.exportIdentity(identity);
    }
}
