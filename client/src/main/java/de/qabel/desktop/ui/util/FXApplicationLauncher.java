package de.qabel.desktop.ui.util;

import com.sun.javafx.application.PlatformImpl;
import javafx.application.Application;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

public class FXApplicationLauncher {
    private static final Logger logger = LoggerFactory.getLogger(FXApplicationLauncher.class);

    public void launch(final Application app) throws Exception {
        AtomicBoolean exited = new AtomicBoolean(false);
        AtomicBoolean started = new AtomicBoolean(false);

        PlatformUtils.start();
        app.init();
        if (!exited.get()) {
            startWithCaution(app, exited, started);
        }

        PlatformUtils.onExit(() -> stop(app, started));
    }

    private void stop(Application app, AtomicBoolean started) {
        if (!started.get()) {
            return;
        }
        try {
            app.stop();
        } catch (Exception e) {
            logger.error("failed to shutdown FX App " + app.getClass().getName(), e);
        }
    }

    private void startWithCaution(Application app, AtomicBoolean exited, AtomicBoolean started)
        throws Exception {
        AtomicReference<Exception> exception = new AtomicReference<>();
        PlatformImpl.runAndWait(() -> {
            try {
                start(app, exited, started);
            } catch (Exception e) {
                exception.set(e);
            }
        });
        if (exception.get() != null) {
            throw exception.get();
        }
    }

    private void start(Application application, AtomicBoolean exited, AtomicBoolean started) throws Exception {
        if (exited.get()) {
            return;
        }

        Stage primaryStage = new Stage();
        primaryStage.impl_setPrimary(true);  // this is only for dev annoyance I think
        application.start(primaryStage);
        started.set(true);
    }
}
