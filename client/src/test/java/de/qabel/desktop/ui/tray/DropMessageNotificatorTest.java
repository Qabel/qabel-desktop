package de.qabel.desktop.ui.tray;

import de.qabel.core.config.Contact;
import de.qabel.core.crypto.QblECKeyPair;
import de.qabel.core.drop.DropMessage;
import de.qabel.desktop.daemon.drop.MessageReceivedEvent;
import de.qabel.desktop.ui.AbstractControllerTest;
import de.qabel.desktop.ui.actionlog.PersistenceDropMessage;
import de.qabel.desktop.ui.actionlog.item.renderer.MessageRendererFactory;
import de.qabel.desktop.util.Translator;
import org.jetbrains.annotations.NotNull;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.mockito.Mockito;
import rx.Scheduler;
import rx.observers.TestSubscriber;
import rx.schedulers.Schedulers;
import rx.schedulers.TestScheduler;
import rx.subjects.TestSubject;

import java.util.ResourceBundle;
import java.util.concurrent.TimeUnit;

public class DropMessageNotificatorTest extends AbstractControllerTest {
    private QabelTray tray = Mockito.mock(QabelTray.class);
    private TestScheduler testScheduler = Schedulers.test();
    private TestSubscriber<TrayNotification> testSubscriber = new TestSubscriber<>();
    private TestSubject<MessageReceivedEvent> eventTestSubject;
    private Contact me = new Contact("the teser", null, new QblECKeyPair().getPub());
    private Contact senderContact = new Contact("senderContact", null, new QblECKeyPair().getPub());
    private Contact senderContact2 = new Contact("senderContact2", null, new QblECKeyPair().getPub());
    private DropMessageNotificator notificator;

    private class Tester extends DropMessageNotificator {

        Tester(QabelTray tray, Scheduler computationScheduler, Scheduler fxScheduler) {
            super(Mockito.mock(MessageRendererFactory.class), Mockito.mock(ResourceBundle.class), Mockito.mock(Translator.class),
                tray, computationScheduler, fxScheduler);
        }

        @Override
        String renderPlaintextMessage(PersistenceDropMessage message) {
            return "plaintext";
        }

        @Override
        String renderTitle(PersistenceDropMessage message) {
            return "title";
        }

    }

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();
        notificator = createNotificator();
    }

    @Test
    public void oneNotification() throws Exception {
        notificator
            .subscribeToEvents(eventTestSubject)
            .subscribe(testSubscriber);

        eventTestSubject.onNext(createNewMessageReceivedEvent(senderContact));

        testScheduler.advanceTimeBy(1, TimeUnit.SECONDS);
        testSubscriber.assertValueCount(1);
    }


    @Ignore
    @Test
    public void groupBySender() {
//        MessageReceivedEvent v = createNewMessageReceivedEvent(senderContact);

        notificator = createNotificator();
//        testScheduler.advanceTimeBy(1, TimeUnit.SECONDS);
        notificator
            .preFilterEvents(eventTestSubject)
            .groupBy(PersistenceDropMessage::getSender)
            .buffer(1, TimeUnit.SECONDS)
            .subscribe(groupedObservables -> {
                System.out.println(groupedObservables.size());
            })
        ;

//        .map(dropMessageGroupedObservable -> {
////                Observable<List<PersistenceDropMessage>> listObservable = dropMessageGroupedObservable.toList();
//            System.out.println("FOO");
//            return null;
//        })


        eventTestSubject.onNext(createNewMessageReceivedEvent(senderContact));
        eventTestSubject.onNext(createNewMessageReceivedEvent(senderContact));

    }

    @NotNull
    private MessageReceivedEvent createNewMessageReceivedEvent(Contact senderContact) {
        DropMessage dropMessage = new DropMessage(senderContact, "payload" + Math.random(), "type");
        PersistenceDropMessage persistenceDropMessage = new PersistenceDropMessage(dropMessage, senderContact, me, false, false);
        return new MessageReceivedEvent(persistenceDropMessage);
    }

    private DropMessageNotificator createNotificator() {
        testScheduler = Schedulers.test();
        eventTestSubject = TestSubject.create(testScheduler);
        notificator = new Tester(tray, testScheduler, testScheduler);
        return notificator;
    }


}
