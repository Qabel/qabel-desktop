package de.qabel.desktop.repository.sqlite.hydrator;

import de.qabel.core.drop.DropURL;
import de.qabel.core.exceptions.QblDropInvalidURL;
import de.qabel.desktop.repository.sqlite.Hydrator;

import java.net.URISyntaxException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

public class DropURLHydrator implements Hydrator<DropURL> {
    @Override
    public String[] getFields() {
        return new String[]{"url"};
    }

    @Override
    public DropURL hydrateOne(ResultSet resultSet) throws SQLException {
        String url = resultSet.getString(1);
        try {
            return new DropURL(url);
        } catch (URISyntaxException | QblDropInvalidURL e) {
            throw new SQLException("failed to hydrate DropUrl from " + url, e);
        }
    }

    @Override
    public Collection<DropURL> hydrateAll(ResultSet resultSet) throws SQLException {
        Set<DropURL> urls = new HashSet<>();
        while (resultSet.next()) {
            urls.add(hydrateOne(resultSet));
        }
        return urls;
    }

    @Override
    public void recognize(DropURL instance) {

    }
}
