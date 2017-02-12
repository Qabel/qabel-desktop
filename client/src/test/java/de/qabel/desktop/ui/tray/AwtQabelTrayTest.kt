package de.qabel.desktop.ui.tray

import de.qabel.desktop.ui.AbstractControllerTest
import javafx.application.Platform
import javafx.stage.Stage
import org.junit.After
import org.junit.Before
import org.junit.Test

class AwtQabelTrayTest : AbstractControllerTest() {
    lateinit var primaryStage: Stage
    lateinit var tray: AwtQabelTray

    @Before
    override fun setUp() {
        super.setUp()
        Platform.setImplicitExit(false)
        runLaterAndWait { primaryStage = Stage() }
        tray = AwtQabelTray(primaryStage, { a, b, c -> Unit })

        runLaterAndWait { primaryStage.show() }
        waitUntil { primaryStage.isShowing }
        runLaterAndWait { }
    }

    @Test
    fun bringToFontBringsIconifiedToFront() {
        runLaterAndWait { primaryStage.isIconified = true }

        tray.bringAppToFront()

        waitUntil { !primaryStage.isIconified }
    }

    @Test
    fun showsClosedStage() {
        runLaterAndWait { primaryStage.close() }

        tray.showApp()

        waitUntil { primaryStage.isShowing }
    }

    @Test
    fun showsIconifiedStage() {
        runLaterAndWait { primaryStage.isIconified = true }

        tray.showApp()

        waitUntil { !primaryStage.isIconified }
    }

    @After
    override fun tearDown() {
        runLaterAndWait { primaryStage.close() }
        runLaterAndWait { }
        super.tearDown()
    }
}
