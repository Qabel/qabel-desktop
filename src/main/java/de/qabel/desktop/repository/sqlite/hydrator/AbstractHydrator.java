package de.qabel.desktop.repository.sqlite.hydrator;

import de.qabel.desktop.repository.sqlite.Hydrator;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

public abstract class AbstractHydrator<T> implements Hydrator<T> {
    @Override
    public String[] getFields(String tableAlias) {
        String[] fields = getFields();
        for (int i = 0; i < fields.length; i++) {
            fields[i] = tableAlias + "." + fields[i];
        }
        return fields;
    }

    protected abstract String[] getFields();

    @Override
    public Collection<T> hydrateAll(ResultSet resultSet) throws SQLException {
        List<T> prefixes = new LinkedList<>();
        while (resultSet.next()) {
            prefixes.add(hydrateOne(resultSet));
        }
        return prefixes;
    }
}
