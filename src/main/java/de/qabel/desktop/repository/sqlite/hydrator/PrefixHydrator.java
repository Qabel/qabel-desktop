package de.qabel.desktop.repository.sqlite.hydrator;

import de.qabel.desktop.repository.sqlite.Hydrator;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

public class PrefixHydrator implements Hydrator<String> {
    @Override
    public String[] getFields() {
        return new String[]{"prefix"};
    }

    @Override
    public String hydrateOne(ResultSet resultSet) throws SQLException {
        return resultSet.getString(1);
    }

    @Override
    public Collection<String> hydrateAll(ResultSet resultSet) throws SQLException {
        List<String> prefixes = new LinkedList<>();
        while (resultSet.next()) {
            prefixes.add(hydrateOne(resultSet));
        }
        return prefixes;
    }

    @Override
    public void recognize(String instance) {

    }
}
