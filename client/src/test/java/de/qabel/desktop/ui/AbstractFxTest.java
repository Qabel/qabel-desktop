package de.qabel.desktop.ui;

import de.qabel.desktop.AsyncUtils;
import de.qabel.desktop.ui.util.PlatformUtils;
import org.junit.BeforeClass;

import java.util.concurrent.Callable;

public class AbstractFxTest {
    @BeforeClass
    public static void setUpClass() throws Exception {
        PlatformUtils.start();
    }

    protected void runLaterAndWait(Runnable runnable) {
        AsyncUtils.runLaterAndWait(runnable);
    }

    public static void waitUntil(Callable<Boolean> evaluate) {
        AsyncUtils.waitUntil(evaluate);
    }

    public static void waitUntil(Callable<Boolean> evaluate, long timeout) {
        AsyncUtils.waitUntil(evaluate, timeout);
    }

    public static void waitUntil(Callable<Boolean> evaluate, Callable<String> errorMessage) {
        AsyncUtils.waitUntil(evaluate, errorMessage);
    }

    public static void waitUntil(Callable<Boolean> evaluate, long timeout, Callable<String> errorMessage) {
        AsyncUtils.waitUntil(evaluate, timeout, errorMessage);
    }
}
