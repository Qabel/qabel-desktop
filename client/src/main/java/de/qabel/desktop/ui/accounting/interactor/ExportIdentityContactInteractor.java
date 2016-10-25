package de.qabel.desktop.ui.accounting.interactor;

import de.qabel.core.config.ContactExportImport;
import de.qabel.core.config.Identity;
import de.qabel.desktop.ui.util.FileChooserFactory;
import org.jetbrains.annotations.NotNull;

import javax.inject.Inject;
import java.util.ResourceBundle;

public class ExportIdentityContactInteractor extends AbstractExportIdentityInteractor {
    @Inject
    public ExportIdentityContactInteractor(ResourceBundle resourceBundle, FileChooserFactory fileChooserFactory) {
        super(fileChooserFactory, resourceBundle);
    }

    @NotNull
    @Override
    protected String getExtensionFilterName() {
        return resourceBundle.getString("qcoExtensionFilterLabel");
    }

    @NotNull
    @Override
    protected String getTitle() {
        return resourceBundle.getString("accountingExportContact");
    }

    @NotNull
    @Override
    protected String getExtension() {
        return ".qco";
    }

    @Override
    protected String convertIdentity(Identity identity) {
        return ContactExportImport.exportIdentityAsContact(identity);
    }
}
