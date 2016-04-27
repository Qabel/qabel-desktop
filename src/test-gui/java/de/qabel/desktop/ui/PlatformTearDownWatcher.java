package de.qabel.desktop.ui;

import javafx.application.Platform;
import javafx.stage.Stage;
import org.junit.AssumptionViolatedException;
import org.junit.rules.TestWatcher;
import org.junit.runner.Description;

public class PlatformTearDownWatcher extends TestWatcher {
    private Stage stage;

    public void setStage(Stage stage) {
        this.stage = stage;
    }

    private void tearDown() {
        if (stage != null) {
            Platform.runLater(() -> { try { stage.close(); } catch (Exception ignored) {}});
        }
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
