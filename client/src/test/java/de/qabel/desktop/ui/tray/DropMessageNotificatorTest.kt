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
import rx.lang.kotlin.observable
import rx.observers.TestSubscriber
import rx.schedulers.Schedulers
import rx.subjects.TestSubject
import java.util.*
import java.util.concurrent.TimeUnit

private val l: Long
    get() {
        val delay1Sec: Long = 1000
        return delay1Sec
    }

class DropMessageNotificatorTest : AbstractControllerTest() {
    private val tray = Mockito.mock(QabelTray::class.java)
    private var testScheduler = Schedulers.test()
    private val testSubscriber = TestSubscriber<TrayNotification>()
    private var eventTestSubject: TestSubject<MessageReceivedEvent> = TestSubject.create<MessageReceivedEvent>(testScheduler)

    private val me = Contact("the teser", null, QblECKeyPair().pub)
    private val senderContact = Contact("senderContact", null, QblECKeyPair().pub)
    private val senderContact2 = Contact("senderContact2", null, QblECKeyPair().pub)

    private var notificator: DropMessageNotificator = Tester(tray, testScheduler, testScheduler)


    private inner class Tester internal constructor(tray: QabelTray, computationScheduler: Scheduler, fxScheduler: Scheduler) : DropMessageNotificator(Mockito.mock(MessageRendererFactory::class.java), Mockito.mock(ResourceBundle::class.java), Mockito.mock(Translator::class.java), tray, computationScheduler, fxScheduler) {

        internal override fun renderPlaintextMessage(message: PersistenceDropMessage): String {
            return "plaintext"
        }

        internal override fun renderTitle(message: PersistenceDropMessage): String {
            return "title"
        }

    }

    @Before
    @Throws(Exception::class)
    override fun setUp() {
        super.setUp()
        notificator = createNotificator()
    }

    @Test
    @Throws(Exception::class)
    fun oneNotification() {
        notificator.subscribeToEvents(eventTestSubject).subscribe(testSubscriber)

        eventTestSubject.onNext(createNewMessageReceivedEvent(senderContact))

        testScheduler.advanceTimeBy(1, TimeUnit.SECONDS)
        testSubscriber.assertValueCount(1)
    }

    fun send(delay: Long, contact: Contact = senderContact) {
        eventTestSubject.onNext(createNewMessageReceivedEvent(contact), delay)
    }

    @Test
    fun groupBySender() {
        notificator
            .preFilterEvents(eventTestSubject)
            .buffer(3, TimeUnit.SECONDS, testScheduler)
            .flatMap {
                val listBySender = it.groupBy { it.sender }
                return@flatMap observable<TrayNotification> { subscriber ->
                    listBySender.entries.forEach {
                        val message: PersistenceDropMessage = it.value.first()
                        val sender: Contact = it.value.first().sender as Contact
                        val alias = sender.alias
                        val size = it.value.size
                        subscriber.onNext(TrayNotification(notificator.renderTitle(message), notificator.renderPlaintextMessage(message)))
                    }
                }
            }
            .subscribe(testSubscriber)


        val delay1Sec: Long = 1000
        send(delay1Sec)
        send(delay1Sec)
        send(delay1Sec, senderContact2)
        testScheduler.advanceTimeBy(3, TimeUnit.SECONDS)
        testSubscriber.assertValueCount(2)


        val delay7Sec: Long = 7000
        send(delay7Sec)
        send(delay7Sec, senderContact2)
        send(delay7Sec, senderContact2)
        send(delay7Sec, senderContact2)
        testScheduler.advanceTimeBy(3, TimeUnit.SECONDS)
        testSubscriber.assertValueCount(2)
    }


    private fun createNewMessageReceivedEvent(senderContact: Contact): MessageReceivedEvent {
        val dropMessage = DropMessage(senderContact, "payload" + Math.random(), "type")
        val persistenceDropMessage = PersistenceDropMessage(dropMessage, senderContact, me, false, false)
        return MessageReceivedEvent(persistenceDropMessage)
    }

    private fun createNotificator(): DropMessageNotificator {

        return notificator
    }


}
