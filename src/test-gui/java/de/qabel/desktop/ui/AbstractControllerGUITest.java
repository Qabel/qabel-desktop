package de.qabel.desktop.ui;

import com.airhacks.afterburner.views.FXMLView;
import javafx.application.Platform;
import javafx.scene.control.Alert;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class AbstractControllerGUITest extends AbstractGuiTest<AlertTestController> {
	@Override
	protected FXMLView getView() {
		return new AlertTestView();
	}

	@Test
	public void testAlertDialog() {
		Platform.runLater(() -> controller.alert("some error message", new Exception("exceptionmessage")));
		waitUntil(() -> controller.alert != null);
		waitUntil(() -> controller.exceptionLabel != null);
		Alert alert = controller.alert;
		try {
			assertEquals("Error", alert.getTitle());
			assertEquals("some error message", alert.getHeaderText());
			assertEquals(Alert.AlertType.ERROR, alert.getAlertType());
			assertEquals("exceptionmessage", controller.exceptionLabel.getText());
		} finally {
			try {
				runLaterAndWait(alert::close);
			} catch (AssertionError e) {
				// window will close anyways
			}
		}
	}
}
