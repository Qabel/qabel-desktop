package de.qabel.desktop;

import de.qabel.core.config.Account;
import de.qabel.desktop.config.ClientConfig;
import javafx.stage.Stage;
import org.junit.Before;
import org.junit.Test;

public class DesktopClientGuiTest extends AbstractKernelTest {

    private ClientConfig config;
    private Stage primaryStage;

    @Before
    public void setUp() throws Exception {
        kernel = new TestKernel("dev");
        kernel.initialize();
        config = kernel.getContainer().getClientConfiguration();
    }

    @Test
    public void primaryStageShowsWhenNoAccount() throws Exception {
        startKernel(kernel);
        waitForPrimaryStage();
        AsyncUtils.assertAsync(primaryStage::isShowing);
    }

    @Test
    public void primaryStageClosesWhenSettingAccount() throws Exception {
        config.setAccount(new Account("provider", "user", "auth"));
        startKernel(kernel);
        waitForPrimaryStage();

        AsyncUtils.waitUntil(() -> !primaryStage.isShowing());
    }

    private void waitForPrimaryStage() {
        AsyncUtils.waitUntil(() -> kernel.getContainer().getPrimaryStage() != null, WAIT_TIMEOUT);
        primaryStage = kernel.getContainer().getPrimaryStage();
    }


}
