package de.qabel.desktop.daemon.management;

public class DownloadStub extends TransactionStub implements Download {
    @Override
    public void setMtime(Long mtime) {
        
    }

    @Override
    public void setSize(long size) {
        this.size = size;
    }
}
