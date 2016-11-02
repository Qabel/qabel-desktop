package de.qabel.desktop.ui.accounting.login

import com.nhaarman.mockito_kotlin.mock
import com.nhaarman.mockito_kotlin.verifyNoMoreInteractions
import com.nhaarman.mockito_kotlin.whenever
import de.qabel.core.accounting.BoxClient
import de.qabel.desktop.AsyncUtils.assertAsync
import de.qabel.desktop.ui.AbstractControllerTest
import javafx.stage.Stage
import org.hamcrest.Matchers.equalTo
import org.junit.Test
import java.util.function.Function

class LoginControllerTest : AbstractControllerTest() {
    val controller: LoginController by lazy {
        runLaterAndWait { Stage().let { diContainer.put("primaryStage", it) } }
        LoginView().presenter
    }
    val boxClientMock = mock<BoxClient>()

    override fun setUp() {
        super.setUp()
        controller.boxClientFactory = Function { boxClientMock }
    }

    @Test
    fun usesTokenIfAvailable() {
        account.token = "token123"

        controller.login()
        waitUntil { controller.buttonBar.isVisible }
        verifyNoMoreInteractions(boxClientMock)
    }

    @Test
    fun setsTokenAfterLoginIfNotAvailable() {
        whenever(boxClientMock.login()).then { controller.server.authToken = "has some token"; Any() }

        controller.login()
        assertAsync({ account.token }, equalTo("has some token"))
    }
}
