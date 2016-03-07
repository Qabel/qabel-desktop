package de.qabel.desktop.daemon.sync.blacklist;

public interface Blacklist {
	/**
	 * returns true if the given filename is blacklisted by this blacklist.
	 * Whether it matches a list of strings or patterns or whatever metric is uses is
	 * left open to the implementation.
	 *
	 * @param filename to check
	 * @return true if the filename is blacklisted
	 */
	boolean matches(String filename);
}
