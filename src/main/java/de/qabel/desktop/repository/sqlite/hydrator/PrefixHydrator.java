package de.qabel.desktop.repository.sqlite.hydrator;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

public class PrefixHydrator extends AbstractHydrator<String> {
    @Override
    public String[] getFields() {
        return new String[]{"prefix"};
    }

    @Override
    public String hydrateOne(ResultSet resultSet) throws SQLException {
        return resultSet.getString(1);
    }

    @Override
    public void recognize(String instance) {

    }
}
