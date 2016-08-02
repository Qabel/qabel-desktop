package de.qabel.desktop.util;

import de.qabel.desktop.Kernel;
import de.qabel.desktop.TestKernel;
import javafx.application.Platform;
import javafx.stage.Stage;

import java.util.concurrent.atomic.AtomicBoolean;

public class KernelTestUtils {
    public static final long WAIT_TIMEOUT = 5000L;
    public final AtomicBoolean exited = new AtomicBoolean(false);
    public TestKernel kernel;

    public void startKernel(Kernel kernel) {
        kernel.setShutdown(() -> exited.set(true));
        new Thread(() -> {
            try {
                kernel.start();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }

    public void tearDown() throws Exception {
        Stage primaryStage = kernel.getContainer().getPrimaryStage();
        if (primaryStage != null) {
            Platform.runLater(primaryStage::close);
        }
    }
}
