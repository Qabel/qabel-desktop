package de.qabel.desktop.ui.util;

import com.sun.javafx.application.PlatformImpl;
import javafx.application.Platform;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicBoolean;

public class PlatformUtils {
    private static AtomicBoolean listenerRegistered = new AtomicBoolean(false);
    private static AtomicBoolean exited = new AtomicBoolean(false);
    private static List<Runnable> idleListeners = new CopyOnWriteArrayList<>();
    private static List<Runnable> exitListeners = new CopyOnWriteArrayList<>();

    public static void start() throws InterruptedException {
        registerExitListener();
        Platform.setImplicitExit(false);
        final CountDownLatch startupLatch = new CountDownLatch(1);
        PlatformImpl.startup(() -> startupLatch.countDown());
        startupLatch.await();
    }

    private static void registerExitListener() {
        if (!listenerRegistered.getAndSet(true)) {
            exited.set(false);
            PlatformImpl.addListener(new PlatformImpl.FinishListener() {
                @Override
                public void idle(boolean implicitExit) {
                    idleListeners.forEach(Runnable::run);

                    if (implicitExit) {
                        exitCalled();
                    }
                }

                @Override
                public void exitCalled() {
                    exited.set(true);
                    exitListeners.forEach(Runnable::run);
                }
            });
        }
    }

    public static void shutdown() {
        if (!hasExited() || System.getSecurityManager() != null) {
            PlatformImpl.tkExit();
        }
        PlatformImpl.exit();
    }

    public static void waitForIdle() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);
        onIdle(latch::countDown);
        latch.await();
    }

    public static void waitForExit() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);
        onExit(latch::countDown);
        latch.await();
    }

    public static void onIdle(Runnable runnable) {
        idleListeners.add(runnable);
    }

    public static void onExit(Runnable runnable) {
        exitListeners.add(runnable);
    }

    public static boolean hasExited() {
        return exited.get();
    }
}
