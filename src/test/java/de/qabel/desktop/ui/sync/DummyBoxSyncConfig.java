package de.qabel.desktop.ui.sync;

import de.qabel.core.config.Account;
import de.qabel.core.config.Identity;
import de.qabel.desktop.config.DefaultBoxSyncConfig;
import de.qabel.desktop.nio.boxfs.BoxFileSystem;

import java.nio.file.Paths;

public class DummyBoxSyncConfig extends DefaultBoxSyncConfig {
    public DummyBoxSyncConfig() {
        super(
            Paths.get("/tmp/wayne"),
            BoxFileSystem.getRoot().resolve("train"),
            new Identity("a", null, null),
            new Account("a", "b", "c")
        );
    }
}
