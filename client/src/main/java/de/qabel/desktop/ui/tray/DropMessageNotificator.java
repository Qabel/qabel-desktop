package de.qabel.desktop.ui.tray;

import de.qabel.core.config.Contact;
import de.qabel.core.drop.DropMessage;
import de.qabel.core.event.EventSource;
import de.qabel.desktop.daemon.drop.MessageReceivedEvent;
import de.qabel.desktop.ui.actionlog.PersistenceDropMessage;
import de.qabel.desktop.ui.actionlog.item.renderer.MessageRendererFactory;
import de.qabel.desktop.util.Translator;
import rx.Observable;
import rx.Scheduler;

import javax.inject.Inject;
import java.util.ResourceBundle;

public class DropMessageNotificator {
    private MessageRendererFactory messageRendererFactory;
    private ResourceBundle resourceBundle;
    private Translator translator;
    private QabelTray tray;
    private Scheduler computationScheduler;
    private Scheduler fxScheduler;

    @Inject
    public DropMessageNotificator(
        MessageRendererFactory messageRendererFactory,
        ResourceBundle resourceBundle,
        Translator translator,
        QabelTray tray,
        Scheduler computationScheduler,
        Scheduler fxScheduler
    ) {
        this.messageRendererFactory = messageRendererFactory;
        this.resourceBundle = resourceBundle;
        this.translator = translator;
        this.tray = tray;
        this.computationScheduler = computationScheduler;
        this.fxScheduler = fxScheduler;
    }

    public void subscribe(EventSource events) {
        Observable<MessageReceivedEvent> eventObservable = events.events(MessageReceivedEvent.class);
        subscribeToEvents(eventObservable)
            .subscribe(tray::showNotification);

    }

    Observable<TrayNotification> subscribeToEvents(Observable<MessageReceivedEvent> eventObservable) {
        return preFilterEvents(eventObservable)
            .observeOn(computationScheduler)
            .map(message -> new TrayNotification(renderTitle(message), renderPlaintextMessage(message)))
            .subscribeOn(fxScheduler);
    }

    Observable<PersistenceDropMessage> preFilterEvents(Observable<MessageReceivedEvent> eventObservable) {
        return eventObservable
            .map(MessageReceivedEvent::getMessage)
            .filter(message -> !message.isSeen() && !message.isSent());
    }


    String renderTitle(PersistenceDropMessage message) {
        return translator.getString("newMessageNotification", ((Contact) message.getSender()).getAlias());
    }

    String renderPlaintextMessage(PersistenceDropMessage message) {
        DropMessage dropMessage = message.getDropMessage();
        return messageRendererFactory
            .getRenderer(dropMessage.getDropPayloadType())
            .renderString(dropMessage.getDropPayload(), resourceBundle);
    }
}
