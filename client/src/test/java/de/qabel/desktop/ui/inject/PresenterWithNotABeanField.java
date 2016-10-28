package de.qabel.desktop.ui.inject;

import javax.inject.Inject;

public class PresenterWithNotABeanField {
    @Inject
    private NotABean name;

    public NotABean getName() {
        return name;
    }
}
