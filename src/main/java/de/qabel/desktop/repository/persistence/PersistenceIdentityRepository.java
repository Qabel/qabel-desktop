package de.qabel.desktop.repository.persistence;

import de.qabel.core.config.Identities;
import de.qabel.core.config.Identity;
import de.qabel.core.config.Persistence;
import de.qabel.desktop.repository.IdentityRepository;
import de.qabel.desktop.repository.exception.EntityNotFoundExcepion;
import de.qabel.desktop.repository.exception.PersistenceException;

import java.util.List;

public class PersistenceIdentityRepository extends AbstractCachedPersistenceRepository<Identity> implements IdentityRepository {
    public PersistenceIdentityRepository(Persistence<String> persistence) {
        super(persistence);
    }

    private Identities identities;

    @Override
    public Identity find(String id) throws EntityNotFoundExcepion, PersistenceException {
        Identities identities = findAll();
        Identity entity = identities.getByKeyIdentifier(id);

        if (entity == null) {
            throw new EntityNotFoundExcepion("No identity found for id " + id);
        }
        return entity;
    }

    @Override
    public synchronized Identities findAll() throws EntityNotFoundExcepion, PersistenceException {
        if (identities == null) {
            List<Identities> identitiesList = persistence.getEntities(Identities.class);
            try {
                identities = identitiesList.get(0);
            } catch (Exception e) {
                identities = new Identities();
                if (persistence.updateOrPersistEntity(identities)) {
                    return identities;
                } else {
                    throw new PersistenceException("Failed to save Entity " + identities + ", reason unknown");
                }

            }
        }
        return identities;
    }

    @Override
    public void save(Identity identity) throws PersistenceException {
        boolean result;
        try {
            findAll().put(identity);
            result = persistence.updateOrPersistEntity(identities);
        } catch (Exception e) {
            throw new PersistenceException("Failed to save Entity " + identities + ": " + e.getMessage(), e);
        }
        if (!result) {
            throw new PersistenceException("Failed to save Entity " + identities + ", reason unknown");
        }
    }
}
