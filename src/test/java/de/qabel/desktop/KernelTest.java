package de.qabel.desktop;

import de.qabel.desktop.inject.DesktopServices;
import javafx.application.Platform;
import javafx.scene.control.ButtonBase;
import javafx.scene.control.DialogPane;
import org.junit.Test;

import java.util.concurrent.atomic.AtomicBoolean;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;

public class KernelTest {
    @Test
    public void initializesTheDIContainer() throws Exception {
        Kernel kernel = new Kernel("dev");
        kernel.initialize();

        DesktopServices container = kernel.getContainer();
        assertThat(container.getThanksFileContent(), is(not("")));
    }

    @Test
    public void testVersionCheckExitsOnRequiredCancel() throws Exception {
        AtomicBoolean exited = new AtomicBoolean(false);

        Kernel kernel = new Kernel("0.0.0");
        kernel.setChecker(FakeUpdateChecker.builder().current("1.0.0").minimum("0.1.0").url("http://download").build());

        kernel.initialize();
        kernel.setShutdown(() -> exited.set(true));
        startKernel(kernel);

        AsyncUtils.waitUntil(() -> kernel.alertRef.get() != null);
        DialogPane dialog = kernel.alertRef.get().getDialogPane();
        Platform.runLater(((ButtonBase) dialog.lookupButton(kernel.cancelButton))::fire);

        AsyncUtils.waitUntil(exited::get);
    }

    @Test
    public void testVersionCheckExitsOnUpdate() throws Exception {
        AtomicBoolean exited = new AtomicBoolean(false);

        Kernel kernel = new Kernel("0.0.0");
        kernel.setChecker(FakeUpdateChecker.builder().current("1.0.0").minimum("0.1.0").url("http://download").build());

        kernel.initialize();
        kernel.setShutdown(() -> exited.set(true));
        startKernel(kernel);

        AsyncUtils.waitUntil(() -> kernel.alertRef.get() != null);
        DialogPane dialog = kernel.alertRef.get().getDialogPane();
        Platform.runLater(((ButtonBase) dialog.lookupButton(kernel.updateButton))::fire);

        AsyncUtils.waitUntil(exited::get);
    }

    public void startKernel(Kernel kernel) {
        new Thread(() -> {
            try {
                kernel.start();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }
}
