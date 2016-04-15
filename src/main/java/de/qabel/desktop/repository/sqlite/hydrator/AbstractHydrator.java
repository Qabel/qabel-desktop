package de.qabel.desktop.repository.sqlite.hydrator;

import de.qabel.desktop.repository.sqlite.Hydrator;

public abstract class AbstractHydrator<T> implements Hydrator<T> {
    @Override
    public String[] getFields(String... tableAlias) {
        String[] fields = getFields();
        for (int i = 0; i < fields.length; i++) {
            fields[i] = tableAlias[0] + "." + fields[i];
        }
        return fields;
    }

    protected abstract String[] getFields();
}
