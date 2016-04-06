package de.qabel.desktop.repository.sqlite.hydrator;

import de.qabel.core.config.Identity;
import de.qabel.core.crypto.QblECKeyPair;
import de.qabel.core.drop.DropURL;
import de.qabel.desktop.config.factory.IdentityFactory;
import de.qabel.desktop.repository.EntityManager;
import de.qabel.desktop.repository.sqlite.Hydrator;
import org.spongycastle.util.encoders.Hex;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;

public class IdentityHydrator implements Hydrator<Identity> {
    private IdentityFactory identityFactory;
    private EntityManager entityManager;

    public IdentityHydrator(IdentityFactory identityFactory, EntityManager entityManager) {
        this.identityFactory = identityFactory;
        this.entityManager = entityManager;
    }

    @Override
    public String[] getFields() {
        return new String[]{"ROWID", "privateKey", "alias", "email", "phone"};
    }

    @Override
    public Identity hydrateOne(ResultSet resultSet) throws SQLException {
        Collection<DropURL> dropUrls = new HashSet<>();
        int i = 1;
        int id = resultSet.getInt(i++);
        if (entityManager.contains(Identity.class, id)) {
            return entityManager.get(Identity.class, id);
        }
        byte[] privateKey = Hex.decode(resultSet.getString(i++));
        String alias = resultSet.getString(i++);
        String email = resultSet.getString(i++);
        String phone = resultSet.getString(i++);
        Identity identity = identityFactory.createIdentity(new QblECKeyPair(privateKey), dropUrls, alias);
        identity.setId(id);
        identity.setEmail(email);
        identity.setPhone(phone);
        return identity;
    }

    @Override
    public Collection<Identity> hydrateAll(ResultSet resultSet) throws SQLException {
        List<Identity> identities = new LinkedList<>();
        while (resultSet.next()) {
            identities.add(hydrateOne(resultSet));
        }
        return identities;
    }

    @Override
    public void recognize(Identity identity) {
        entityManager.put(Identity.class, identity);
    }
}
