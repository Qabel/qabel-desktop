package de.qabel.desktop.ui.tray

import de.qabel.core.event.EventSource
import de.qabel.core.logging.QabelLog
import de.qabel.desktop.ServiceFactory
import de.qabel.desktop.event.ClientStartedEvent
import de.qabel.desktop.event.MainStageShownEvent
import de.qabel.desktop.inject.AnnotatedDesktopServiceFactory
import de.qabel.desktop.inject.CompositeServiceFactory
import de.qabel.desktop.inject.Create
import de.qabel.desktop.plugin.ClientPlugin
import de.qabel.desktop.plugin.ServiceFactoryProvider
import de.qabel.desktop.repository.DropMessageRepository
import de.qabel.desktop.ui.actionlog.item.renderer.FXMessageRendererFactory
import de.qabel.desktop.util.Translator
import javafx.application.Platform
import javafx.stage.Stage
import rx.Scheduler
import java.util.*
import javax.inject.Inject

class TrayPlugin : ClientPlugin, ServiceFactoryProvider, QabelLog {
    @Inject
    private lateinit var dropMessageRepository: DropMessageRepository
    @Inject
    private lateinit var trayFactory: (Stage) -> QabelTray
    @Inject
    private lateinit var messageRendererFactory: FXMessageRendererFactory
    @Inject
    private lateinit var translator: Translator
    @Inject
    private lateinit var tray: TrayProxy
    @Inject
    private lateinit var resourceBundle: ResourceBundle
    @Inject
    private lateinit var dropMessageNotificator: DropMessageNotificator
    @Inject
    private lateinit var fxScheduler: Scheduler

    override fun getServiceFactory(): ServiceFactory = TrayServiceFactory()

    override fun initialize(serviceFactory: CompositeServiceFactory, events: EventSource) {
        installTray(events)
        dropMessageNotificator.subscribe(events)
        minimizeMainStage(events)
    }

    private fun minimizeMainStage(events: EventSource) = events.events(MainStageShownEvent::class.java)
        .subscribe { event -> Platform.runLater { event.primaryStage.isIconified = true } }

    private fun installTray(events: EventSource) = events.events(ClientStartedEvent::class.java)
        .subscribe { event ->
            try {
                tray.instance = trayFactory(event.primaryStage)
                tray.install()
            } catch (e: Exception) {
                error("failed to install tray", e)
            }
        }
}
class TrayServiceFactory : AnnotatedDesktopServiceFactory() {
    val tray = TrayProxy()
        @Create(name = "tray") get

    val trayFactory: (Stage) -> QabelTray = { stage -> AwtQabelTray(stage, toastStrategy) }
        @Create(name = "trayFactory") get

    val toastStrategy: ToastStrategy = AwtToast()
        @Create(name = "toastStrategy") get
}
