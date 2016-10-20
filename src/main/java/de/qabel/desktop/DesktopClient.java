package de.qabel.desktop;

import org.apache.commons.io.IOUtils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;


public class DesktopClient {
    public static void main(String[] args) throws Exception {
        String version = appVersion();
        if (args.length > 0) {
            if (args[0].equals("--version")) {
                System.out.println(appVersion());
                System.exit(-1);
            }
        }


        Path qabelConfigRoot = Paths.get(System.getProperty("user.home")).resolve(".qabel");
        System.setProperty("log.root", qabelConfigRoot.toAbsolutePath().toString());

        Kernel kernel = new Kernel(version);

        if (args.length > 0) {
            kernel.setDatabaseFile(new File(args[0]).getAbsoluteFile().toPath());
        }
        kernel.initialize();
        kernel.start();
    }

    public static String appVersion() throws IOException {
        return IOUtils.toString(DesktopClient.class.getResourceAsStream("/version")).trim();
    }
}
