package de.qabel.desktop.daemon.sync;

public interface BoxSync {
    boolean isSynced();
    double getProgress();
    int countFiles();
    int countFolders();
    boolean hasError();
}
