package de.qabel.desktop;

import cucumber.api.java.After;
import javafx.application.Platform;
import javafx.stage.Stage;

import java.util.concurrent.atomic.AtomicBoolean;

class AbstractKernelTest {
    static final long WAIT_TIMEOUT = 5000L;
    final AtomicBoolean exited = new AtomicBoolean(false);
    TestKernel kernel;

    void startKernel(Kernel kernel) {
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
