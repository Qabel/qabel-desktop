package de.qabel.desktop.debug;

import de.qabel.desktop.DesktopClient;
import de.qabel.desktop.Kernel;
import org.scenicview.ScenicView;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;

public class DebugClient extends DesktopClient {
    public static void main(String[] args) throws Exception {
        Path qabelConfigRoot = Paths.get(System.getProperty("user.home")).resolve(".qabel");
        System.setProperty("log.root", qabelConfigRoot.toAbsolutePath().toString());

        Kernel kernel = new Kernel("dev");

        if (args.length > 0) {
            kernel.setDatabaseFile(new File(args[0]).getAbsoluteFile().toPath());
        }
        kernel.initialize();
        kernel.getApp().setPostInit(app -> ScenicView.show(app.getPrimaryStage().getScene()));
        kernel.start();
    }
}
