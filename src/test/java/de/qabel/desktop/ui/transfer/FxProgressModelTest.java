package de.qabel.desktop.ui.transfer;

import com.sun.javafx.robot.impl.BaseFXRobot;
import de.qabel.desktop.ui.AbstractControllerTest;
import javafx.application.Platform;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

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

    @Test
    public void hasMinimumUpdateFrequency() throws Exception {
        model.setMinimumUpdateDelay(1000L);
        progress.setProgress(0.1);
        progress.totalSize = 100;
        model.setProgress(progress);
        progress.setProgress(0.2);
        assertProgress(0.1);
    }

    @Test
    public void ignoresMinimumUpdateFrequencyOnFinish() throws Exception {
        model.setMinimumUpdateDelay(1000L);
        progress.setProgress(0.1);
        progress.totalSize = 100;
        model.setProgress(progress);
        progress.currentSize = 100;
        progress.setProgress(0.2);
        assertProgress(0.2);
    }
}
