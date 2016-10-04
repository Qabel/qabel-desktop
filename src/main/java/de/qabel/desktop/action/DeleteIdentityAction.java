package de.qabel.desktop.action;

import de.qabel.core.config.Identity;
import de.qabel.core.event.EventSink;
import de.qabel.core.repository.IdentityRepository;
import de.qabel.core.repository.exception.PersistenceException;

import javax.inject.Inject;

public class DeleteIdentityAction {
    @Inject
    private IdentityRepository identityRepository;

    @Inject
    private EventSink eventSink;

    @Inject
    public DeleteIdentityAction(IdentityRepository identityRepository, EventSink eventSink) {
        this.identityRepository = identityRepository;
        this.eventSink = eventSink;
    }

    public void delete(Identity identity) throws PersistenceException {
        identityRepository.delete(identity);
        eventSink.push(new IdentityDeletedEvent(identity));
    }
}
