package de.qabel.desktop.ui.tray

import de.qabel.desktop.ui.AbstractControllerTest
import javafx.stage.Stage
import org.junit.After
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class AwtQabelTrayTest : AbstractControllerTest() {
    lateinit var primaryStage: Stage
    lateinit var tray: AwtQabelTray

    @Before
    override fun setUp() {
        super.setUp()
        runLaterAndWait { primaryStage = Stage() }
        tray = AwtQabelTray(primaryStage, { _, _, _ -> Unit })
    }

    @Test
    fun bringToFontBringsIconifiedToFront() {
        runLaterAndWait {
            primaryStage.show()
            primaryStage.isIconified = true
        }

        tray.bringAppToFront()
        runLaterAndWait {  }

        assertFalse(primaryStage.isIconified)
    }

    @Test
    fun showsClosedStage() {
        runLaterAndWait { primaryStage.isIconified = true }

        tray.showApp()
        runLaterAndWait {  }

        assertTrue(primaryStage.isShowing)
        assertFalse(primaryStage.isIconified)
    }

    @After
    override fun tearDown() {
        runLaterAndWait { primaryStage.close() }
        super.tearDown()
    }
}
