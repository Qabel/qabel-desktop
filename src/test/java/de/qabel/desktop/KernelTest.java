package de.qabel.desktop;

import com.sun.javafx.application.PlatformImpl;
import de.qabel.desktop.inject.DesktopServices;
import javafx.application.Platform;
import javafx.scene.control.ButtonBase;
import javafx.scene.control.ButtonType;
import javafx.scene.control.DialogPane;
import javafx.stage.Stage;
import org.junit.After;
import org.junit.Test;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;

public class KernelTest {
    public static final long WAIT_TIMEOUT = 5000L;
    private final AtomicBoolean exited = new AtomicBoolean(false);
    private Kernel kernel;

    @Test
    public void initializesTheDIContainer() throws Exception {
        kernel = new TestKernel("dev");
        kernel.initialize();

        DesktopServices container = kernel.getContainer();
        assertThat(container.getAboutFilesContent().thanksFileContent, is(not("")));
    }

    @Test
    public void testVersionCheckExitsOnRequiredCancel() throws Exception {
        kernel = new TestKernel("0.0.0");
        kernel.setChecker(FakeUpdateChecker.builder().current("1.0.0").minimum("0.1.0").url("http://download").build());

        kernel.initialize();
        startKernel(kernel);

        reactOnVersionAlert(kernel.cancelButton);

        AsyncUtils.waitUntil(exited::get, WAIT_TIMEOUT);
    }

    @Test
    public void testVersionCheckExitsOnUpdate() throws Exception {
        kernel = new TestKernel("0.0.0");
        kernel.setChecker(FakeUpdateChecker.builder().current("1.0.0").minimum("0.1.0").url("http://download").build());

        AtomicReference<String> document = new AtomicReference<>();
        kernel.initialize();
        kernel.documentLauncher = document::set;
        startKernel(kernel);

        reactOnVersionAlert(kernel.updateButton);

        AsyncUtils.waitUntil(exited::get, WAIT_TIMEOUT);
        assertThat(document.get(), is("http://download"));
    }

    @Test
    public void testVersionCheckLaunchesOnUnrequiredCancel() throws Exception {
        kernel = new TestKernel("0.1.0");
        kernel.setChecker(FakeUpdateChecker.builder().current("1.0.0").minimum("0.1.0").url("http://download").build());

        kernel.initialize();
        startKernel(kernel);

        reactOnVersionAlert(kernel.cancelButton);

        AsyncUtils.waitUntil(() -> kernel.getContainer().getPrimaryStage() != null);
        assertThat(exited.get(), is(false));
    }

    public void reactOnVersionAlert(ButtonType button) {
        AsyncUtils.waitUntil(() -> kernel.alertRef.get() != null, WAIT_TIMEOUT);
        DialogPane dialog = kernel.alertRef.get().getDialogPane();
        PlatformImpl.runAndWait(() -> ((ButtonBase) dialog.lookupButton(button)).fire());
    }

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

    @After
    public void tearDown() throws Exception {
        Stage primaryStage = kernel.getContainer().getPrimaryStage();
        if (primaryStage != null) {
            Platform.runLater(primaryStage::close);
        }
    }
}
