package de.qabel.desktop;

import de.qabel.core.config.Account;
import javafx.application.Platform;
import javafx.stage.Stage;
import org.junit.After;
import org.junit.Test;

import java.util.concurrent.atomic.AtomicBoolean;

public class DesktopClientGuiTest {

    private TestKernel kernel;
    private final AtomicBoolean exited = new AtomicBoolean(false);

    @Test
    public void notLoggedIn() throws Exception {
        startTestKernel();
        AsyncUtils.waitUntil(() -> kernel.app.config != null, 5000L);
        kernel.app.config.onSetAccount(null);
        Stage primaryStage = kernel.getContainer().getPrimaryStage();
        AsyncUtils.assertAsync(primaryStage::isShowing);
    }

    @Test
    public void loggedIn() throws Exception {
        startTestKernel();
        AsyncUtils.waitUntil(() -> kernel.app.config != null, 5000L);
        kernel.app.config.onSetAccount(account -> new Account("provider", "user", "auth"));
        Stage primaryStage = kernel.getContainer().getPrimaryStage();
        AsyncUtils.waitUntil(() -> !primaryStage.isShowing());
    }

    private void startTestKernel() throws Exception {
        kernel = new TestKernel("dev");
        kernel.initialize();
        startKernel(kernel);
    }

    private void startKernel(Kernel kernel) {
        kernel.setShutdown(() -> exited.set(true));
        new Thread(() -> {
            try {
                kernel.start();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }

    @After
    public void tearDown() throws Exception {
        Stage primaryStage = kernel.getContainer().getPrimaryStage();
        if (primaryStage != null) {
            Platform.runLater(primaryStage::close);
        }
    }

}
