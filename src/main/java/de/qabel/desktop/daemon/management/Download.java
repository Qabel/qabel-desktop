package de.qabel.desktop.daemon.management;

public interface Download extends Transaction {
	void setMtime(Long mtime);
}
