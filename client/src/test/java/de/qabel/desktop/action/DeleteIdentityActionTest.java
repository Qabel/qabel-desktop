package de.qabel.desktop.action;

import de.qabel.core.config.Identity;
import de.qabel.core.config.factory.DropUrlGenerator;
import de.qabel.core.config.factory.IdentityBuilderFactory;
import de.qabel.core.event.Event;
import de.qabel.core.event.EventDispatcher;
import de.qabel.core.event.SubjectEventDispatcher;
import de.qabel.core.repository.IdentityRepository;
import de.qabel.core.repository.inmemory.InMemoryIdentityRepository;
import org.junit.Before;
import org.junit.Test;
import rx.observers.TestSubscriber;

import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.instanceOf;
import static org.junit.Assert.assertFalse;

public class DeleteIdentityActionTest {
    private TestSubscriber<Event> subscriber = new TestSubscriber<>();
    private IdentityRepository repo = new InMemoryIdentityRepository();
    private Identity identity;
    private DeleteIdentityAction action;

    @Before
    public void setUp() throws Exception {
        identity = new IdentityBuilderFactory(new DropUrlGenerator("http://localhost:5000")).factory()
            .withAlias("tester").build();
        repo.save(identity);
        EventDispatcher dispatcher = new SubjectEventDispatcher();
        dispatcher.getEvents().subscribe(subscriber);
        action = new DeleteIdentityAction(repo, dispatcher);
    }

    @Test
    public void deletesIdentity() throws Exception {
        action.delete(identity);

        assertFalse(repo.findAll().getIdentities().contains(identity));
        List<Event> events = subscriber.getOnNextEvents();
        assertThat(events, hasSize(1));
        assertThat(events.get(0), instanceOf(IdentityDeletedEvent.class));
    }
}
