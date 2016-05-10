package de.qabel.desktop.repository.inmemory;

import de.qabel.core.config.Identities;
import de.qabel.core.config.Identity;
import de.qabel.desktop.repository.IdentityRepository;
import de.qabel.desktop.repository.exception.EntityNotFoundException;
import de.qabel.desktop.repository.exception.PersistenceException;

import java.util.List;
import java.util.stream.Collectors;

public class InMemoryIdentityRepository implements IdentityRepository {
    private Identities identities = new Identities();

    @Override
    public Identity find(String id) throws EntityNotFoundException {
        if (identities.getByKeyIdentifier(id) == null) {
            throw new EntityNotFoundException("id " + id + " not found");
        }
        return identities.getByKeyIdentifier(id);
    }

    @Override
    public Identity find(int id) throws EntityNotFoundException, PersistenceException {
        for (Identity identity : identities.getIdentities()) {
            if (identity.getId() == id) {
                return identity;
            }
        }
        throw new EntityNotFoundException("fail");
    }

    @Override
    public Identities findAll() throws EntityNotFoundException {
        return identities;
    }

    @Override
    public void save(Identity identity) throws PersistenceException {
        if (identity.getId() == 0) {
            identity.setId(identities.getIdentities().size() + 1);
        }
        if (!identities.contains(identity)) {
            identities.put(identity);
        }
    }

    public void clear() {
        List<Identity> oldIdentities = identities.getIdentities().stream().collect(Collectors.toList());
        oldIdentities.forEach(identities::remove);
    }
}
