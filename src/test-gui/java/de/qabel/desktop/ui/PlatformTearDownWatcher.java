package de.qabel.desktop.ui;

import de.qabel.desktop.AsyncUtils;
import javafx.application.Platform;
import javafx.stage.Stage;
import javafx.stage.Window;
import org.junit.AssumptionViolatedException;
import org.junit.rules.TestWatcher;
import org.junit.runner.Description;
import org.testfx.api.FxRobot;

public class PlatformTearDownWatcher extends TestWatcher {
    private Stage stage;
    private FxRobot robot;

    public PlatformTearDownWatcher(FxRobot robot) {
        this.robot = robot;
    }

    public void setStage(Stage stage) {
        this.stage = stage;
    }

    private void tearDown() {
        if (stage != null) {
            Platform.runLater(() -> { try { stage.close(); } catch (Exception ignored) {}});
        }
        /*for (Window window : robot.robotContext().getWindowFinder().listTargetWindows()) {
            AsyncUtils.runLaterAndWait(window::hide);
        }
        stage = null;
        System.gc();*/
    }

    @Override
    protected void succeeded(Description description) {
        tearDown();
    }

    @Override
    protected void failed(Throwable e, Description description) {
        tearDown();
    }

    @Override
    protected void skipped(AssumptionViolatedException e, Description description) {
        tearDown();
    }
}
