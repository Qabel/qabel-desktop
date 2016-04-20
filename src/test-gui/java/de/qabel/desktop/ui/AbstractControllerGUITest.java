package de.qabel.desktop.ui;

import com.airhacks.afterburner.views.FXMLView;
import javafx.application.Platform;
import javafx.scene.control.Alert;
import org.junit.Test;

import static de.qabel.desktop.AsyncUtils.waitUntil;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class AbstractControllerGUITest extends AbstractGuiTest<AlertTestController> {
    @Override
    protected FXMLView getView() {
        return new AlertTestView();
    }

    @Test
    public void
    testAlertDialog() throws Exception {
        Platform.runLater(() -> controller.alert("some error message", new Exception("exceptionmessage")));
        waitUntil(() -> controller.alert != null);
        waitUntil(() -> controller.alert.getExceptionLabel() != null);
        waitForNode(".feedback");
        CrashReportAlert alert = controller.alert;
        try {
            waitTillTheEnd(alert.getAlert().getDialogPane());
            assertEquals("Error", alert.getAlert().getTitle());
            assertEquals("some error message", alert.getAlert().getHeaderText());
            assertEquals(Alert.AlertType.ERROR, alert.getAlert().getAlertType());
            assertEquals("exceptionmessage", alert.getExceptionLabel().getText());

            clickOn(".feedback").write("123456");
            clickOn(".send");
            waitUntil(alert.getInputArea().getText()::isEmpty);
            assertEquals("123456", crashReportHandler.text);
            assertNotNull(crashReportHandler.stacktrace);
        } finally {
            try {
                runLaterAndWait(alert::close);
            } catch (AssertionError e) {
                // window will close anyways
            }
        }
    }
}
