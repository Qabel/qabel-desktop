package de.qabel.desktop.daemon.sync.worker;

import de.qabel.desktop.daemon.sync.blacklist.Blacklist;

import java.nio.file.Path;
import java.util.LinkedList;
import java.util.List;

public class BlacklistSpy implements Blacklist {
    private Blacklist blacklist;
    public List<Path> tests = new LinkedList<>();
    public int matches;

    public BlacklistSpy(Blacklist blacklist) {
        this.blacklist = blacklist;
    }

    @Override
    public boolean matches(Path path) {
        tests.add(path);
        boolean match = blacklist.matches(path);
        if (match) {
            ++matches;
        }
        return match;
    }
}
