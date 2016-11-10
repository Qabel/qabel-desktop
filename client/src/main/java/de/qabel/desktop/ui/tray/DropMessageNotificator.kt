package de.qabel.desktop.ui.tray

import de.qabel.core.config.Contact
import de.qabel.core.event.EventSource
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
            .flatMap(createCombinedNotification())

            .observeOn(computationScheduler)
            .subscribeOn(fxScheduler)
    }

    private fun createCombinedNotification(): (MutableList<PersistenceDropMessage>) -> Observable<TrayNotification> {
        return {
            observable { subscriber ->
                if (it.size > 1) {
                    val listBySender = it.groupBy { it.sender }
                    if (listBySender.isNotEmpty()) {
                        subscriber.onNext(multipleSenderNotifications(listBySender.values))
                    } else {
                        subscriber.onNext(simpleSingleNotification(it[0]))
                    }
                } else {
                    subscriber.onNext(simpleSingleNotification(it[0]))
                }

            }
        }
    }

    private fun simpleSingleNotification(message: PersistenceDropMessage): TrayNotification {
        return TrayNotification(renderTitle(message), renderPlaintextMessage(message))
    }

    private fun multipleSenderNotifications(listBySender: Collection<List<PersistenceDropMessage>>): TrayNotification? {
        listBySender.forEach { messages ->
            val message: PersistenceDropMessage = messages.first()
            if (messages.size > 1) {
                return TrayNotification(renderTitle(message), renderMultiMessageBody(messages))
            } else {
                return simpleSingleNotification(message)
            }
        }
        return null
    }

    internal open fun renderMultiMessageBody(messages: List<PersistenceDropMessage>): String {
        val count = messages.size
        val sender = (messages.first().sender as Contact).alias
        return translator.getString("newMultipleMessagesNotification", count, sender)
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

    open fun renderMutlipleMessages(messages: List<PersistenceDropMessage>): String {
        throw UnsupportedOperationException()
    }
}
