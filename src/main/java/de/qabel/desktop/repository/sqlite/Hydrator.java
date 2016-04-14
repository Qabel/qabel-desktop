package de.qabel.desktop.repository.sqlite;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

public interface Hydrator<T> {
    String[] getFields(String... tableAlias);
    T hydrateOne(ResultSet resultSet) throws SQLException;

    /**
     * force the hydrator to know the instance (and to return it on future hydrates or add it to an EntityManager etc)
     */
    void recognize(T instance);

    default Collection<T> hydrateAll(ResultSet resultSet) throws SQLException {
        List<T> prefixes = new LinkedList<>();
        while (resultSet.next()) {
            prefixes.add(hydrateOne(resultSet));
        }
        return prefixes;
    }
}
