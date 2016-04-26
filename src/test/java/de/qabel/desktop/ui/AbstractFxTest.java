package de.qabel.desktop.ui;

import com.sun.javafx.application.PlatformImpl;
import de.qabel.desktop.AsyncUtils;
import javafx.application.Application;
import javafx.application.Platform;
import org.junit.BeforeClass;

import java.lang.reflect.Field;
import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicBoolean;

public class AbstractFxTest {
    @BeforeClass
    public static void setUpClass() throws Exception {
        Platform.setImplicitExit(false);
        try {
            Platform.runLater(() -> {
            });
        } catch (IllegalStateException e) {
            startPlatform();
        } catch (Exception e) {
            e.printStackTrace();
            startPlatform();
        }
    }

    private static void startPlatform() throws NoSuchFieldException, IllegalAccessException, InterruptedException {
        new Thread() {
            @Override
            public void run() {
                Application.launch(TestApplication.class);
            }
        }.start();
        Field field = PlatformImpl.class.getDeclaredField("initialized");
        field.setAccessible(true);
        while (!((AtomicBoolean) field.get(null)).get())
            Thread.sleep(10);
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
