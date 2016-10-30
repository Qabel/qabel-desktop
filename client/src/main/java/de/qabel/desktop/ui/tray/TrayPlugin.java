package de.qabel.desktop.ui.tray;

import de.qabel.core.config.Contact;
import de.qabel.core.drop.DropMessage;
import de.qabel.core.event.EventSource;
import de.qabel.desktop.ClientPlugin;
import de.qabel.desktop.event.ClientStartedEvent;
import de.qabel.desktop.inject.AnnotatedDesktopServiceFactory;
import de.qabel.desktop.inject.CompositeServiceFactory;
import de.qabel.desktop.inject.Create;
import de.qabel.desktop.repository.DropMessageRepository;
import de.qabel.desktop.ui.actionlog.PersistenceDropMessage;
import de.qabel.desktop.ui.actionlog.item.renderer.FXMessageRenderer;
import de.qabel.desktop.ui.actionlog.item.renderer.FXMessageRendererFactory;
import de.qabel.desktop.ui.inject.AfterburnerInjector;
import de.qabel.desktop.util.Translator;
import javafx.application.Platform;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.util.ResourceBundle;
import java.util.function.Function;

public class TrayPlugin implements ClientPlugin {
    private static final Logger logger = LoggerFactory.getLogger(TrayPlugin.class);

    @Inject
    private DropMessageRepository dropMessageRepository;

    @Inject
    private Function<Stage, QabelTray> trayFactory;

    @Inject
    private FXMessageRendererFactory messageRendererFactory;

    @Inject
    private Translator translator;

    @Inject
    private TrayProxy tray;

    @Inject
    private ResourceBundle resourceBundle;

    @Override
    public void initialize(CompositeServiceFactory serviceFactory, EventSource events) {
        serviceFactory.addServiceFactory(new TrayServiceFactory());
        AfterburnerInjector.injectMembers(this);

        events.events().filter(e -> e instanceof ClientStartedEvent)
            .map(e -> (ClientStartedEvent)e)
            .subscribe(event -> {
                try {
                    tray.setInstance(trayFactory.apply(event.getPrimaryStage()));
                    tray.install();
                } catch (Exception e) {
                    logger.error("failed to install tray", e);
                }
            });


        dropMessageRepository.addObserver(
            (o, arg) -> {
                if (!(arg instanceof PersistenceDropMessage)) {
                    return;
                }
                PersistenceDropMessage message = (PersistenceDropMessage) arg;
                if (message.isSent() || message.isSeen()) {
                    return;
                }
                Contact sender = (Contact) message.getSender();
                DropMessage dropMessage = message.getDropMessage();
                FXMessageRenderer renderer = messageRendererFactory
                    .getRenderer(dropMessage.getDropPayloadType());
                String content = renderer
                    .renderString(dropMessage.getDropPayload(), resourceBundle);
                String title = translator.getString("newMessageNotification", sender.getAlias());
                Platform.runLater(() -> tray.showNotification(title, content));
            }
        );
    }

    private class TrayServiceFactory extends AnnotatedDesktopServiceFactory {
        private TrayProxy trayProxy = new TrayProxy();

        @Create(name = "tray")
        public TrayProxy getTray() {
            return trayProxy;
        }

        @Create(name = "trayFactory")
        public Function<Stage, QabelTray> getTrayFactory() {
            return stage -> new AwtQabelTray(stage, getToastStrategy());
        }

        @Create(name = "toastStrategy")
        public ToastStrategy getToastStrategy() {
            return new AwtToast();
        }
    }
}
