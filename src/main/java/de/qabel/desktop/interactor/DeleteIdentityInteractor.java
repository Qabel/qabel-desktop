package de.qabel.desktop.interactor;

import de.qabel.core.config.Identity;
import de.qabel.core.repository.IdentityRepository;
import de.qabel.core.repository.exception.PersistenceException;
import de.qabel.desktop.event.EventDispatcher;
import de.qabel.desktop.event.identity.IdentitiesChangedEvent;
import de.qabel.desktop.event.identity.IdentityChangedEvent;

import javax.inject.Inject;

public class DeleteIdentityInteractor {
    @Inject
    private IdentityRepository identityRepository;

    @Inject
    private EventDispatcher eventDispatcher;

    @Inject
    public DeleteIdentityInteractor(IdentityRepository identityRepository, EventDispatcher eventDispatcher) {
        this.identityRepository = identityRepository;
        this.eventDispatcher = eventDispatcher;
    }

    public void delete(Identity identity) throws PersistenceException {
        identityRepository.delete(identity);
        eventDispatcher.push(new IdentityDeletedEvent(identity));
    }

    public class IdentityDeletedEvent implements IdentitiesChangedEvent, IdentityChangedEvent {
        private Identity identity;

        public IdentityDeletedEvent(Identity identity) {
            this.identity = identity;
        }

        @Override
        public Identity getIdentity() {
            return identity;
        }
    }
}
