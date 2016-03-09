package de.qabel.desktop.daemon.sync.blacklist;

import java.nio.file.Path;

public interface Blacklist {
	/**
	 * returns true if the given filename is blacklisted by this blacklist.
	 * Whether it matches a list of strings or patterns or whatever metric is uses is
	 * left open to the implementation.
	 *
	 * @param path to check
	 * @return true if the filename is blacklisted
	 */
	boolean matches(Path path);
}
