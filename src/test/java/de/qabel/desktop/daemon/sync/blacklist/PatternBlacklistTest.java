package de.qabel.desktop.daemon.sync.blacklist;

import org.junit.Test;

import java.nio.file.Paths;
import java.util.regex.Pattern;

import static org.junit.Assert.*;

public class PatternBlacklistTest {
    private PatternBlacklist blacklist = new PatternBlacklist();

    @Test
    public void doesntMatchWithoutPattern() {
        assertFalse(blacklist.matches(Paths.get("filename")));
    }

    @Test
    public void doesMatchWithMatchingPattern() {
        blacklist.add(Pattern.compile(".*"));
        assertTrue(blacklist.matches(Paths.get("filename")));
    }

    @Test
    public void doesMatchWithMatchingFolder() {
        blacklist.add(Pattern.compile(".*a.*"));
        assertTrue(blacklist.matches(Paths.get("das/xxx")));
    }

    @Test
    public void matchesIfAtLeastOnePatternMatches() {
        blacklist.add(Pattern.compile("doesntmatch"));
        blacklist.add(Pattern.compile("f.*"));
        assertTrue(blacklist.matches(Paths.get("filename")));
    }

    @Test
    public void doesntMatchIfNotWhileStringMatches() {
        blacklist.add(Pattern.compile("[0-9]+"));
        assertFalse(blacklist.matches(Paths.get("f0")));
    }

    @Test
    public void matchesIfAtLeastFirstPatternMatches() {
        blacklist.add(Pattern.compile("f.*"));
        blacklist.add(Pattern.compile("doesntmatch"));
        assertTrue(blacklist.matches(Paths.get("filename")));
    }
}
