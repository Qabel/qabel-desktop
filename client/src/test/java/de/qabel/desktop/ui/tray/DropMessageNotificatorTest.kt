package de.qabel.desktop.ui.tray

import de.qabel.core.config.Contact
import de.qabel.core.crypto.QblECKeyPair
import de.qabel.core.drop.DropMessage
import de.qabel.desktop.daemon.drop.MessageReceivedEvent
import de.qabel.desktop.daemon.drop.TextMessage
import de.qabel.desktop.ui.AbstractControllerTest
import de.qabel.desktop.ui.actionlog.PersistenceDropMessage
import de.qabel.desktop.util.Translator
import de.qabel.desktop.util.UTF8Converter
import junit.framework.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito
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

    private val resourceBundle: ResourceBundle = ResourceBundle.getBundle("ui", Locale("te", "ST"), UTF8Converter())
    private val translator: Translator = Translator(resourceBundle)

    private lateinit var notificator: DropMessageNotificator

    private val me = Contact("the teser", null, QblECKeyPair().pub)
    private val senderContact = Contact("senderContact", null, QblECKeyPair().pub)
    private val senderContact2 = Contact("senderContact2", null, QblECKeyPair().pub)

    val delay1Sec: Long = 1000
    val delay6Sec: Long = 6000
    @Before
    @Throws(Exception::class)
    override fun setUp() {
        super.setUp()
        notificator = DropMessageNotificator(
            fxMessageRendererFactory,
            resourceBundle, translator,
            tray, testScheduler, testScheduler
        )

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
        testSubscriber.onNextEvents.single().apply {
            assertEquals("New message from senderContact:", this.title)
            assertEquals("myChatDropMessagePayload", this.content)
        }
    }

    @Test
    fun multipleMessagesByOneSender() {
        sendEvent(delay1Sec)
        sendEvent(delay1Sec)
        testScheduler.advanceTimeBy(3, TimeUnit.SECONDS)
        testSubscriber.assertValueCount(1)
        testSubscriber.onNextEvents.single().apply {
            assertEquals("New message from senderContact:", this.title)
            assertEquals("2 messages from senderContact", this.content)
        }
    }

    @Test
    fun oneMessageByTwoSenders() {
        sendEvent(delay1Sec)
        sendEvent(delay1Sec, senderContact2)
        testScheduler.advanceTimeBy(3, TimeUnit.SECONDS)
        testSubscriber.assertValueCount(1)
        testSubscriber.onNextEvents.single().apply {
            assertEquals("2 new  messages", this.title)
            assertEquals("2 messages from senderContact, senderContact2", this.content)
        }
    }


    @Test
    fun multipleMessagesInMultipleTimeframe() {

        sendEvent(delay1Sec, senderContact2)
        sendEvent(delay1Sec, senderContact2)

        sendEvent(delay6Sec)
        sendEvent(delay6Sec)
        sendEvent(delay6Sec, senderContact2)
        sendEvent(delay6Sec, senderContact2)

        testScheduler.advanceTimeTo(3, TimeUnit.SECONDS)
        testSubscriber.assertValueCount(1)
        testSubscriber.onNextEvents.single().apply {
            assertEquals("New message from senderContact2:", this.title)
            assertEquals("2 messages from senderContact2", this.content)
            testSubscriber.assertValuesAndClear(this)
        }

        testScheduler.advanceTimeTo(6, TimeUnit.SECONDS)
        testSubscriber.assertValueCount(1)
        testSubscriber.onNextEvents.single().apply {
            assertEquals("4 new  messages", this.title)
            assertEquals("4 messages from senderContact, senderContact2", this.content)
        }

    }

    fun sendEvent(delay: Long, contact: Contact = senderContact) = eventTestSubject.onNext(createNewMessageReceivedEvent(contact), delay)

    internal fun createNewMessageReceivedEvent(senderContact: Contact): MessageReceivedEvent {
        val persistenceDropMessage = createPersistenceDropMessage(createDropMessage(senderContact), senderContact)
        return MessageReceivedEvent(persistenceDropMessage)
    }

    internal fun createPersistenceDropMessage(dropMessage: DropMessage, senderContact: Contact) = PersistenceDropMessage(dropMessage, senderContact, me, false, false)

    internal fun createDropMessage(senderContact: Contact) = DropMessage(senderContact, TextMessage("myChatDropMessagePayload").toJson(), "type")

}
