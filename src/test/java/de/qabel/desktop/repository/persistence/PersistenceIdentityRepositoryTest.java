package de.qabel.desktop.repository.persistence;

import de.qabel.core.config.Identities;
import de.qabel.core.config.Identity;
import de.qabel.core.config.Persistence;
import de.qabel.desktop.config.factory.DropUrlGenerator;
import de.qabel.desktop.config.factory.IdentityBuilder;
import de.qabel.desktop.repository.exception.EntityNotFoundException;
import de.qabel.desktop.repository.exception.PersistenceException;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class PersistenceIdentityRepositoryTest extends AbstractPersistenceRepositoryTest<PersistenceIdentityRepository> {

    @Test
    public void returnsEmptyListWithoutInstances() throws Exception {
        Identities results = repo.findAll();
        assertEquals(0, results.getIdentities().size());
    }

    @Test
    public void returnsPersistedEntities() throws Exception {
        Identity identity = new IdentityBuilder(new DropUrlGenerator("http://localhost:5000")).withAlias("a").build();
        Identities identities = new Identities();
        identities.put(identity);
        persistence.persistEntity(identities);
        Identities results = repo.findAll();

        assertEquals(1, results.getIdentities().size());
        assertEquals(identity, results.getByKeyIdentifier(identity.getKeyIdentifier()));
    }

    @Test(expected = EntityNotFoundException.class)
    public void throwsExcetionIfEntityIsNotFound() throws Exception {
        repo.find("1");
    }

    @Test
    public void findsPersistedEntity() throws Exception {
        Identity identity = new IdentityBuilder(new DropUrlGenerator("http://localhost:5000")).withAlias("a").build();
        Identities identities = new Identities();
        identities.put(identity);
        persistence.persistEntity(identities);

        Identity id = repo.find(identity.getKeyIdentifier());
        assertEquals(identity, id);
    }

    @Override
    protected PersistenceIdentityRepository createRepository(Persistence<String> persistence) {
        return new PersistenceIdentityRepository(persistence);
    }

    @Test
    public void savesNewInstances() throws Exception {
        Identity identity = new IdentityBuilder(new DropUrlGenerator("http://localhost:5000")).withAlias("a").build();
        repo.save(identity);
        assertEquals(identity, repo.find(identity.getKeyIdentifier()));
    }

    @Test
    public void updatesExistingInstances() throws Exception {
        Identity identity = new IdentityBuilder(new DropUrlGenerator("http://localhost:5000")).withAlias("a").build();
        repo.save(identity);
        String keyId = identity.getKeyIdentifier();

        identity.setAlias("b");
        repo.save(identity);

        assertEquals(identity, repo.find(keyId));
    }

    @Test(expected = PersistenceException.class)
    public void throwsPersistenceExceptionOnFailure() throws Exception {
        repo.save(null);
    }
}
