package de.qabel.desktop.ui;

import com.airhacks.afterburner.views.FXMLView;
import javafx.scene.control.Alert;
import org.junit.Test;

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
        controller.alert("some error message", new Exception("exceptionmessage"));
        waitUntil(() -> controller.alert != null);
        waitUntil(() -> controller.alert.getExceptionLabel() != null);
        waitForNode(".feedback");
        CrashReportAlert alert = controller.alert;
        try {
            runLaterAndWait(alert.getAlert().getDialogPane()::requestFocus);
            CrashReportAlertPage page = new CrashReportAlertPage(baseFXRobot, robot, alert);

            assertEquals("Error", page.getTitle());
            assertEquals("some error message", page.getHeaderText());
            assertEquals(Alert.AlertType.ERROR, page.getType());
            assertEquals("exceptionmessage", page.getExceptionLabel());

            page.setFeedback("123456").send();
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
