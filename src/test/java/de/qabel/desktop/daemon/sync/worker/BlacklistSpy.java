package de.qabel.desktop.daemon.sync.worker;

import de.qabel.desktop.daemon.sync.blacklist.Blacklist;

import java.util.LinkedList;
import java.util.List;

public class BlacklistSpy implements Blacklist {
	private Blacklist blacklist;
	public List<String> tests = new LinkedList<>();
	public int matches = 0;

	public BlacklistSpy(Blacklist blacklist) {
		this.blacklist = blacklist;
	}

	@Override
	public boolean matches(String filename) {
		tests.add(filename);
		boolean match = blacklist.matches(filename);
		if (match) {
			++matches;
		}
		return match;
	}
}
