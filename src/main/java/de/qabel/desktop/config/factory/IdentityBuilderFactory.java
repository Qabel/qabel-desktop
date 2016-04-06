package de.qabel.desktop.config.factory;

import javax.inject.Inject;

public class IdentityBuilderFactory {
    private DropUrlGenerator dropUrlGenerator;

    @Inject
    public IdentityBuilderFactory(DropUrlGenerator dropUrlGenerator) {
        this.dropUrlGenerator = dropUrlGenerator;
    }

    public IdentityBuilder factory() {
        return new IdentityBuilder(dropUrlGenerator);
    }
}
