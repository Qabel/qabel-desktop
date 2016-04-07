package de.qabel.desktop.inject;

import de.qabel.desktop.config.factory.DefaultIdentityFactory;
import de.qabel.desktop.config.factory.IdentityFactory;
import de.qabel.desktop.inject.config.RuntimeConfiguration;
import de.qabel.desktop.repository.EntityManager;
import de.qabel.desktop.repository.IdentityRepository;
import de.qabel.desktop.repository.sqlite.SqliteIdentityDropUrlRepository;
import de.qabel.desktop.repository.sqlite.SqliteIdentityRepository;
import de.qabel.desktop.repository.sqlite.SqlitePrefixRepository;
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

    private IdentityHydrator getIdentityHydrator() {
        return new IdentityHydrator(
            getIdentityFactory(),
            getEntityManager(),
            getIdentityDropUrlRepository(),
            getPrefixRepository()
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

    private IdentityFactory identityFactory = new DefaultIdentityFactory();
    public IdentityFactory getIdentityFactory() {
        return identityFactory;
    }

    private EntityManager entityManager = new EntityManager();
    public EntityManager getEntityManager() {
        return entityManager;
    }
}
