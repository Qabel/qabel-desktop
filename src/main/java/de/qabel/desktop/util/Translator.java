package de.qabel.desktop.util;

import java.text.MessageFormat;
import java.util.ResourceBundle;

public class Translator {
    private ResourceBundle resourceBundle;

    public Translator(ResourceBundle resourceBundle) {
        this.resourceBundle = resourceBundle;
    }

    public String getString(String message, Object... params) {
        return MessageFormat.format(resourceBundle.getString(message), params);
    }
}
