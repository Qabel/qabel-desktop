package de.qabel.desktop.daemon.sync.blacklist;

import org.junit.Before;
import org.junit.Test;

import java.nio.file.Paths;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class FileBasedSyncBlacklistTest {
    private Blacklist blacklist;

    @Before
    public void setUp() throws Exception {
        blacklist = new FileBasedSyncBlacklist(Paths.get(getClass().getResource("/ignore").toURI()));
    }

    @Test
    public void testExcludesDownload() {
        assertTrue(blacklist.matches(Paths.get(".mydoc.docx.qpart~")));
    }

    @Test
    public void testDoesNotExcludeNormalWordDocument() {
        assertFalse(blacklist.matches(Paths.get("mydoc.docx")));
    }
}
