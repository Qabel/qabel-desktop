package de.qabel.desktop.ui.tray

import com.airhacks.afterburner.views.QabelFXMLView
import de.qabel.desktop.event.ClientStartedEvent
import de.qabel.desktop.event.MainStageShownEvent
import de.qabel.desktop.ui.AbstractControllerTest
import de.qabel.desktop.ui.inject.AfterburnerInjector
import javafx.stage.Stage
import org.junit.After
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import rx.schedulers.JavaFxScheduler
import rx.schedulers.Schedulers

class TrayPluginTest : AbstractControllerTest() {
    lateinit var plugin: TrayPlugin
    lateinit var primaryStage: Stage
    val stubTray = StubQabelTray()
    val trayFactory: (Stage) -> QabelTray = { stubTray }

    @Before
    override fun setUp() {
        super.setUp()
        diContainer.put("trayFactory", trayFactory)
        dropMessageNotificator = DropMessageNotificator(
            fxMessageRendererFactory,
            QabelFXMLView.getDefaultResourceBundle(),
            translator,
            stubTray,
            Schedulers.immediate(),
            JavaFxScheduler.getInstance()
        )
        diContainer.put("dropMessageNotificator", dropMessageNotificator)
        runLaterAndWait {
            primaryStage = Stage()
        }
        plugin = TrayPlugin().apply {
            diContainer.addServiceFactory(getServiceFactory())
            AfterburnerInjector.injectMembers(this, diContainer::get)
            initialize(diContainer, eventDispatcher)
        }
    }

    @Test
    fun minimizes() {
        runLaterAndWait {
            primaryStage.isIconified = false
            primaryStage.show()
        }

        eventDispatcher.push(MainStageShownEvent(primaryStage))

        waitUntil { primaryStage.isIconified == true }
        assertTrue(primaryStage.isShowing)
    }

    @Test
    fun installs() {
        eventDispatcher.push(ClientStartedEvent(primaryStage))

        waitUntil { stubTray.installed }
    }

    @After
    override fun tearDown() {
        runLaterAndWait(primaryStage::close)
        super.tearDown()
    }
}
