package de.qabel.desktop.ui.tray

import de.qabel.core.config.Contact
import de.qabel.core.event.EventSource
import de.qabel.core.util.DefaultHashMap
import de.qabel.desktop.daemon.drop.MessageReceivedEvent
import de.qabel.desktop.ui.actionlog.PersistenceDropMessage
import de.qabel.desktop.ui.actionlog.item.renderer.MessageRendererFactory
import de.qabel.desktop.util.Translator
import rx.Observable
import rx.Scheduler
import rx.lang.kotlin.observable
import java.util.*
import java.util.concurrent.TimeUnit
import javax.inject.Inject

open class DropMessageNotificator
@Inject
constructor(
    private val messageRendererFactory: MessageRendererFactory,
    private val resourceBundle: ResourceBundle,
    private val translator: Translator,
    private val tray: QabelTray,
    private val computationScheduler: Scheduler,
    private val fxScheduler: Scheduler
) {

    fun subscribe(events: EventSource) {
        val eventObservable = events.events(MessageReceivedEvent::class.java)
        subscribeToEvents(eventObservable)
            .subscribe({ tray.showNotification(it) })
    }

    internal fun subscribeToEvents(eventObservable: Observable<MessageReceivedEvent>): Observable<TrayNotification> {
        return eventObservable
            .map { it.message }
            .filter { message -> !message.isSeen && !message.isSent }
            .buffer(3, TimeUnit.SECONDS, computationScheduler)
            .filter { it.size > 0 }
            .flatMap(createCombinedNotification())
            .observeOn(computationScheduler)
            .subscribeOn(fxScheduler)
    }

    private fun createCombinedNotification(): (MutableList<PersistenceDropMessage>) -> Observable<TrayNotification> {

        return {
            observable { subscriber ->
                val messages = it
                countMessagesByContact(messages).let { byContact ->
                    val firstMessage = messages.first()
                    if (byContact.size > 1) {
                        val header = renderMultiMsgTitle(messages.size)
                        val body = renderMultiMessageBody(messages.size, byContact)
                        subscriber.onNext(TrayNotification(header, body))
                    } else {
                        if (messages.size > 1) {
                            val body = renderMultiMessageBody(messages.size, byContact)
                            subscriber.onNext(TrayNotification(renderTitle(firstMessage), body))
                        } else {
                            subscriber.onNext(createSingleMessageNotification(firstMessage))
                        }
                    }
                }
            }
        }
    }

    internal open fun renderMultiMsgTitle(size: Int): String {
        return translator.getString("newMultipleMessagesNotificationTitle", size)
    }

    internal open fun createSingleMessageNotification(message: PersistenceDropMessage): TrayNotification {
        return TrayNotification(renderTitle(message), renderPlaintextMessage(message))
    }

    private fun countMessagesByContact(messages: List<PersistenceDropMessage>): DefaultHashMap<Contact, Int> =
        DefaultHashMap<Contact, Int>({ 0 }).apply {
            messages.forEach {
                val c = it.sender as Contact
                put(c, getOrDefault(c).plus(1))
            }
        }

    internal open fun renderMultiMessageBody(msgCount: Int, byContact: DefaultHashMap<Contact, Int>): String {
        val contactsCombined = byContact.keys.map { it.alias }.sorted().joinToString(",")
        return translator.getString("newMultipleMessagesNotification", msgCount, contactsCombined)
    }

    internal open fun renderTitle(message: PersistenceDropMessage): String {
        return translator.getString("newMessageNotification", (message.sender as Contact).alias)
    }

    internal open fun renderPlaintextMessage(message: PersistenceDropMessage): String {
        val dropMessage = message.dropMessage
        return messageRendererFactory
            .getRenderer(dropMessage.dropPayloadType)
            .renderString(dropMessage.dropPayload, resourceBundle)
    }

}
