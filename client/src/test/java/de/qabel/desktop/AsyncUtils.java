package de.qabel.desktop;

import de.qabel.desktop.ui.AbstractController;
import de.qabel.desktop.ui.AbstractControllerTest;
import javafx.application.Platform;
import org.hamcrest.Matcher;

import java.util.concurrent.Callable;

import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

public class AsyncUtils {
    public static void waitUntil(Callable<Boolean> evaluate) {
        waitUntil(evaluate, 2000L);
    }

    public static void waitUntil(Callable<Boolean> evaluate, long timeout) {
        waitUntil(evaluate, timeout, () -> "wait timeout");
    }

    public static void waitUntil(Callable<Boolean> evaluate, Callable<String> errorMessage) {
        waitUntil(evaluate, 2000L, errorMessage);
    }

    public static void waitUntil(Callable<Boolean> evaluate, long timeout, Callable<String> errorMessage) {
        long startTime = System.currentTimeMillis();
        try {
            while (!evaluate.call()) {
                Thread.yield();
                Thread.sleep(10);
                if (System.currentTimeMillis() - timeout > startTime) {
                    fail(errorMessage.call());
                }
            }
        } catch (Exception e) {
            AbstractControllerTest.createLogger().error(e.getMessage(), e);
            fail(e.getMessage());
        }
    }

    public static void runLaterAndWait(Runnable runnable) {
        boolean[] hasRun = new boolean[]{false};
        Platform.runLater(() -> {
            runnable.run();
            hasRun[0] = true;
        });
        waitUntil(() -> hasRun[0], 5000L);
    }

    public static void assertAsync(AbstractController.CheckedRunnable assertion) {
        assertAsync(assertion, 2000L);
    }

    public static void assertAsync(AbstractController.CheckedRunnable assertion, long timeout) {
        long startTime = System.currentTimeMillis();
        while (true) {
            try {
                assertion.run();
                return;
            } catch(AssertionError e){
                if (System.currentTimeMillis() > startTime + timeout) {
                    throw e;
                }
                Thread.yield();
                try { Thread.sleep(10); } catch (InterruptedException ignored) {}
            } catch (Exception e) {
                AbstractControllerTest.createLogger().error(e.getMessage(), e);
                fail(e.getMessage());
            }
        }
    }

    public static <T> void assertAsync(Callable<T> actual, Matcher<? super T> matcher) {
        assertAsync(() -> assertThat(actual.call(), matcher));
    }

    public static <T> void assertAsync(Callable<T> actual, Matcher<? super T> matcher, long timeout) {
        assertAsync(() -> assertThat(actual.call(), matcher), timeout);
    }
}
