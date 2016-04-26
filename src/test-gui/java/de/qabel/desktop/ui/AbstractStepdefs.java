package de.qabel.desktop.ui;

import java.util.concurrent.Callable;

public abstract class AbstractStepdefs<T extends AbstractController> extends AbstractGuiTest<T> {
    @Override
    public void setUp() throws Exception {
        AbstractFxTest.setUpClass();
        super.setUp();
    }

    public static void waitUntil(Callable<Boolean> evaluate) {
        AbstractFxTest.waitUntil(evaluate, 15000L);
    }
}
