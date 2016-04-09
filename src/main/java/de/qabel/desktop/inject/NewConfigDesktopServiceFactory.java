package de.qabel.desktop.inject;

import de.qabel.desktop.config.factory.ContactFactory;
import de.qabel.desktop.config.factory.DefaultContactFactory;
import de.qabel.desktop.config.factory.DefaultIdentityFactory;
import de.qabel.desktop.config.factory.IdentityFactory;
import de.qabel.desktop.inject.config.RuntimeConfiguration;
import de.qabel.desktop.repository.ContactRepository;
import de.qabel.desktop.repository.EntityManager;
import de.qabel.desktop.repository.IdentityRepository;
import de.qabel.desktop.repository.sqlite.*;
import de.qabel.desktop.repository.sqlite.hydrator.ContactHydrator;
import de.qabel.desktop.repository.sqlite.hydrator.DropURLHydrator;
import de.qabel.desktop.repository.sqlite.hydrator.IdentityHydrator;

public class NewConfigDesktopServiceFactory extends StaticDesktopServiceFactory {
    public NewConfigDesktopServiceFactory(RuntimeConfiguration runtimeConfiguration) {
        super(runtimeConfiguration);
    }

    @Override
    public synchronized IdentityRepository getIdentityRepository() {
        if (identityRepository == null) {
            identityRepository = new SqliteIdentityRepository(
                runtimeConfiguration.getConfigDatabase(),
                getIdentityHydrator(),
                getIdentityDropUrlRepository(),
                getPrefixRepository()
            );
        }
        return identityRepository;
    }

    @Override
    public synchronized ContactRepository getContactRepository() {
        if (contactRepository == null) {
            contactRepository = new SqliteContactRepository(
                runtimeConfiguration.getConfigDatabase(),
                getContactHydrator(),
                getContactDropUrlRepository()
            );
        }
        return contactRepository;
    }

    private IdentityHydrator getIdentityHydrator() {
        return new IdentityHydrator(
            getIdentityFactory(),
            getEntityManager(),
            getIdentityDropUrlRepository(),
            getPrefixRepository()
        );
    }

    private ContactHydrator getContactHydrator() {
        return new ContactHydrator(
            getEntityManager(),
            getContactFactory(),
            getContactDropUrlRepository()
        );
    }

    private SqlitePrefixRepository prefixRepository;
    private synchronized SqlitePrefixRepository getPrefixRepository() {
        if (prefixRepository == null) {
            prefixRepository = new SqlitePrefixRepository(runtimeConfiguration.getConfigDatabase());
        }
        return prefixRepository;
    }

    private SqliteIdentityDropUrlRepository dropUrlRepository;
    private synchronized SqliteIdentityDropUrlRepository getIdentityDropUrlRepository() {
        if (dropUrlRepository == null) {
            dropUrlRepository = new SqliteIdentityDropUrlRepository(
                runtimeConfiguration.getConfigDatabase(),
                new DropURLHydrator()
            );
        }
        return dropUrlRepository;
    }

    private SqliteContactDropUrlRepository contactDropUrlRepository;
    private synchronized SqliteContactDropUrlRepository getContactDropUrlRepository() {
        if (contactDropUrlRepository == null) {
            contactDropUrlRepository = new SqliteContactDropUrlRepository(
                runtimeConfiguration.getConfigDatabase(),
                new DropURLHydrator()
            );
        }
        return contactDropUrlRepository;
    }

    private IdentityFactory identityFactory = new DefaultIdentityFactory();
    public IdentityFactory getIdentityFactory() {
        return identityFactory;
    }

    private ContactFactory contactFactory = new DefaultContactFactory();
    public ContactFactory getContactFactory() {
        return contactFactory;
    }

    private EntityManager entityManager = new EntityManager();
    public EntityManager getEntityManager() {
        return entityManager;
    }
}
