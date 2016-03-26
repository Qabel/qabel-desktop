package de.qabel.desktop.ui.transfer;

import com.sun.javafx.robot.impl.BaseFXRobot;
import de.qabel.desktop.ui.AbstractControllerTest;
import javafx.application.Platform;
import org.junit.Test;

import static de.qabel.desktop.AsyncUtils.waitUntil;
import static org.junit.Assert.*;

public class FxProgressModelTest extends AbstractControllerTest {
	private ProgressStub progress = new ProgressStub();
	private FxProgressModel model = new FxProgressModel();

	@Override
	public void setUp() throws Exception {
		super.setUp();
		model.setMinimumUpdateDelay(null);
	}

	@Test
	public void givenNoProgressAllIsDone() {
		assertProgress(1.0);
	}

	private void assertProgress(double expected) {
		new BaseFXRobot(null).waitForIdle();
		assertEquals(expected, model.progressProperty().get(), 0.001);
	}

	@Test
	public void setsFromProgress() {
		model.setProgress(progress);
		assertProgress(0.0);
	}

	@Test
	public void updatesOnProgress() {
		model.setProgress(progress);
		progress.setProgress(0.5);
		assertProgress(0.5);
	}

	@Test
	public void updatesFromCurrentProgress() {
		model.setProgress(progress);
		progress.setProgress(1.0);
		ProgressStub progress2 = new ProgressStub();
		progress2.setProgress(0.5);
		model.setProgress(progress2);

		progress.setProgress(0.0);
		assertProgress(0.5);
	}

	@Test
	public void updatesInFxThread() {
		final double[] updatedProgress = {0.0};
		model.progressProperty().addListener((observable, oldValue, newValue) -> {
			if (!Platform.isFxApplicationThread()) {
				fail("update to " + newValue + " was not in fx thread");
			}
			updatedProgress[0] = (double) newValue;
		});

		model.setProgress(progress);
		progress.setProgress(0.5);
		waitUntil(() -> updatedProgress[0] == 0.5, () -> "progress is " + updatedProgress[0]);
	}
}
