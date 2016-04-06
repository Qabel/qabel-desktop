package de.qabel.desktop.daemon.sync.blacklist;

import java.nio.file.Path;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Pattern;

public class PatternBlacklist implements Blacklist {
    private List<Pattern> patterns = new LinkedList<>();

    @Override
    public boolean matches(Path path) {
        for (int i = 0; i < path.getNameCount(); i++) {
            for (Pattern pattern : patterns) {
                if (pattern.matcher(path.getName(i).toString()).matches()) {
                    return true;
                }
            }
        }
        return false;
    }

    public void add(Pattern pattern) {
        patterns.add(pattern);
    }
}
