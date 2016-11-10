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
            .flatMap {
                val listBySender = it.groupBy { it.sender }
                return@flatMap observable<TrayNotification> { subscriber ->
                    listBySender.entries.forEach {
                        val message: PersistenceDropMessage = it.value.first()
                        val sender: Contact = it.value.first().sender as Contact
                        val alias = sender.alias
                        val size = it.value.size
                        subscriber.onNext(TrayNotification(renderTitle(message), renderPlaintextMessage(message)))
                    }
                }
            }

            .observeOn(computationScheduler)
            .subscribeOn(fxScheduler)
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
