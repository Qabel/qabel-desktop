package de.qabel.desktop;

import de.qabel.desktop.config.LaunchConfig;

public class TestKernel extends Kernel {
    public TestKernel(String currentVersion) {
        super(currentVersion);
    }

    public TestKernel(String currentVersion, LaunchConfig launchConfig) {
        super(currentVersion, launchConfig);
    }

    @Override
    protected String getSqliteConnectionString() {
        return "jdbc:sqlite::memory:";
    }
}
