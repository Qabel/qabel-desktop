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

public class DropURLHydrator extends AbstractHydrator<DropURL> {
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
    public void recognize(DropURL instance) {

    }
}
