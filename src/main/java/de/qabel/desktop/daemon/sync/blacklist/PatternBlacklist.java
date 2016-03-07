package de.qabel.desktop.daemon.sync.blacklist;

import java.util.LinkedList;
import java.util.List;
import java.util.regex.Pattern;

public class PatternBlacklist implements Blacklist {
	private List<Pattern> patterns = new LinkedList<>();

	@Override
	public boolean matches(String filename) {
		for (Pattern pattern : patterns) {
			if (pattern.matcher(filename).matches()) {
				return true;
			}
		}
		return false;
	}

	public void add(Pattern pattern) {
		patterns.add(pattern);
	}
}
