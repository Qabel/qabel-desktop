package de.qabel.desktop.ui.inject;

public class NotABean {
    @SuppressWarnings(value = "unused")
    private int someProperty;

    public NotABean(int someProperty) {
        this.someProperty = someProperty;
    }
}
