package de.qabel.desktop.ui;

import de.qabel.desktop.AsyncUtils;
import javafx.stage.Window;
import org.junit.rules.TestWatcher;
import org.junit.runner.Description;
import org.testfx.api.FxRobot;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.*;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class AScreenshotOnFailureWatcher extends TestWatcher {
    private FxRobot robot;

    public AScreenshotOnFailureWatcher(FxRobot robot) {
        this.robot = robot;
    }

    @Override
    protected void failed(Throwable e, Description description) {
        AsyncUtils.runLaterAndWait(() -> {
            Window window = robot.targetWindow();
            double x = window.getX();
            double y = window.getY();
            double width = window.getWidth();
            double height = window.getHeight();
            try {
                BufferedImage cap = new Robot().createScreenCapture(
                    new Rectangle((int)x, (int)y, (int)width, (int)height)
                );

                Path screenshotDir = Paths.get(".")
                    .resolve("build")
                    .resolve("reports")
                    .resolve("guiTest")
                    .resolve("screenshots");
                if (!Files.isDirectory(screenshotDir)) {
                    Files.createDirectories(screenshotDir);
                }
                ImageIO.write(
                    cap,
                    "png",
                    new File(
                        "build/reports/guiTest/screenshots/"
                            + description.getClassName()
                            + "."
                            + description.getMethodName()
                            + ".png"
                    )
                );
            } catch (Exception e1) {
                AbstractControllerTest.createLogger().error("failed to take screenshot", e1);
            }
        });
    }
}
