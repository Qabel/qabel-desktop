package de.qabel.desktop.repository.sqlite;

import de.qabel.core.config.Identity;
import de.qabel.core.config.factory.DropUrlGenerator;
import de.qabel.core.config.factory.IdentityBuilder;
import de.qabel.core.repository.EntityManager;
import org.junit.Test;

import java.net.URISyntaxException;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class EntityManagerTest {
    private EntityManager em = new EntityManager();

    @Test
    public void containsNothingOnStart() {
        assertFalse(em.contains(Identity.class, 1));
    }

    @Test
    public void containsContainedEntity() throws URISyntaxException {
        Identity identity = new IdentityBuilder(new DropUrlGenerator("http://localhost")).withAlias("test").build();
        identity.setId(1);
        em.put(Identity.class, identity);
        assertTrue(em.contains(Identity.class, 1));
    }

    @Test
    public void containsNothingAfterClear() throws Exception {
        Identity identity = new IdentityBuilder(new DropUrlGenerator("http://localhost")).withAlias("test").build();
        identity.setId(1);
        em.put(Identity.class, identity);
        em.clear();
        assertFalse(em.contains(Identity.class, 1));
    }
}
