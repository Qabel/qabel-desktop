package de.qabel.desktop.ui.tray

import de.qabel.core.config.Contact
import de.qabel.core.crypto.QblECKeyPair
import de.qabel.core.drop.DropMessage
import de.qabel.desktop.daemon.drop.MessageReceivedEvent
import de.qabel.desktop.ui.AbstractControllerTest
import de.qabel.desktop.ui.actionlog.PersistenceDropMessage
import de.qabel.desktop.ui.actionlog.item.renderer.MessageRendererFactory
import de.qabel.desktop.util.Translator
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito
import rx.Scheduler
import rx.observers.TestSubscriber
import rx.schedulers.Schedulers
import rx.subjects.TestSubject
import java.util.*
import java.util.concurrent.TimeUnit

class DropMessageNotificatorTest : AbstractControllerTest() {
    private val tray = Mockito.mock(QabelTray::class.java)
    private var testScheduler = Schedulers.test()
    private val testSubscriber = TestSubscriber<TrayNotification>()
    private var eventTestSubject: TestSubject<MessageReceivedEvent> = TestSubject.create<MessageReceivedEvent>(testScheduler)

    private var notificator: DropMessageNotificator = Tester(tray, testScheduler, testScheduler)

    private val me = Contact("the teser", null, QblECKeyPair().pub)
    private val senderContact = Contact("senderContact", null, QblECKeyPair().pub)
    private val senderContact2 = Contact("senderContact2", null, QblECKeyPair().pub)

    val delay1Sec: Long = 1000
    val delay7Sec: Long = 7000

    private inner class Tester
    internal constructor(tray: QabelTray, computationScheduler: Scheduler, fxScheduler: Scheduler) :
        DropMessageNotificator(Mockito.mock(MessageRendererFactory::class.java),
            Mockito.mock(ResourceBundle::class.java), Mockito.mock(Translator::class.java),
            tray, computationScheduler, fxScheduler) {

        override fun renderPlaintextMessage(message: PersistenceDropMessage): String {
            return "plaintext"
        }

        override fun renderTitle(message: PersistenceDropMessage): String {
            return "title"
        }

        override fun renderMultiMessageBody(messages: List<PersistenceDropMessage>): String {
            return "multi"
        }
    }

    @Before
    @Throws(Exception::class)
    override fun setUp() {
        super.setUp()

        notificator
            .subscribeToEvents(eventTestSubject)
            .subscribe(testSubscriber)
    }

    @Test
    @Throws(Exception::class)
    fun oneMessageByOneSender() {
        sendEvent(delay1Sec)
        testScheduler.advanceTimeBy(3, TimeUnit.SECONDS)
        testSubscriber.assertValueCount(1)
    }

    @Test
    fun multipleMessagesByOneSender() {
        sendEvent(delay1Sec)
        sendEvent(delay1Sec)
        testScheduler.advanceTimeBy(3, TimeUnit.SECONDS)
        testSubscriber.assertValueCount(1)
    }

    @Test
    fun oneMessageByTwoSenders() {
        sendEvent(delay1Sec)
        sendEvent(delay1Sec, senderContact2)
        testScheduler.advanceTimeBy(3, TimeUnit.SECONDS)
        testSubscriber.assertValueCount(2)
    }

    fun sendEvent(delay: Long, contact: Contact = senderContact) = eventTestSubject.onNext(createNewMessageReceivedEvent(contact), delay)

    internal fun createNewMessageReceivedEvent(senderContact: Contact): MessageReceivedEvent {
        val persistenceDropMessage = createPersistenceDropMessage(createDropMessage(senderContact), senderContact)
        return MessageReceivedEvent(persistenceDropMessage)
    }

    internal fun createPersistenceDropMessage(dropMessage: DropMessage, senderContact: Contact) = PersistenceDropMessage(dropMessage, senderContact, me, false, false)

    internal fun createDropMessage(senderContact: Contact) = DropMessage(senderContact, "payload" + Math.random(), "type")


}
