package de.qabel.desktop;

import com.sun.javafx.application.PlatformImpl;
import de.qabel.desktop.inject.DesktopServices;
import javafx.scene.control.ButtonBase;
import javafx.scene.control.ButtonType;
import javafx.scene.control.DialogPane;
import org.junit.Test;

import java.util.concurrent.atomic.AtomicReference;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;

public class KernelTest extends AbstractKernelTest {

    @Test
    public void initializesTheDIContainer() throws Exception {
        kernel = new TestKernel("dev");
        kernel.initialize();

        DesktopServices container = kernel.getContainer();
        assertThat(container.getAboutFilesContent().thanksFileContent, is(not("")));
    }

    @Test
    public void testVersionCheckExitsOnRequiredCancel() throws Exception {
        kernel = new TestKernel("0.0.1");
        kernel.setChecker(FakeUpdateChecker.builder().current("1.0.0").minimum("0.1.0").url("http://download").build());

        kernel.initialize();
        startKernel(kernel);

        reactOnVersionAlert(kernel.cancelButton);

        AsyncUtils.waitUntil(exited::get, WAIT_TIMEOUT);
    }

    @Test
    public void testVersionCheckExitsOnUpdate() throws Exception {
        kernel = new TestKernel("0.0.1");
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

}
