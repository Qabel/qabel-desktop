package de.qabel.desktop.ui;

import javafx.application.Platform;

import java.util.concurrent.Callable;

public abstract class AbstractStepdefs<T extends AbstractController> extends AbstractGuiTest<T> {
    @Override
    public void setUp() throws Exception {
        AbstractFxTest.setUpClass();
        super.setUp();
    }

    @Override
    public void tearDown() throws Exception {
        super.tearDown();
        if (stage != null) {
            Platform.runLater(() -> {try { stage.close(); } catch (Exception ignored) {}});
        }
    }

    public static void waitUntil(Callable<Boolean> evaluate) {
        AbstractFxTest.waitUntil(evaluate, 30000L);
    }
}
