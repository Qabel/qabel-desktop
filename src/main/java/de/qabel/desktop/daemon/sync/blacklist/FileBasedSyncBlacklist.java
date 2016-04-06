package de.qabel.desktop.daemon.sync.blacklist;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

public class FileBasedSyncBlacklist extends PatternBlacklist {
    private static final Logger logger = LoggerFactory.getLogger(FileBasedSyncBlacklist.class.getSimpleName());
    private static final Pattern TMP_DOWNLOAD_PATTERN = Pattern.compile("\\..*\\.qpart~");

    public FileBasedSyncBlacklist(Path patterns) throws IOException {
        add(TMP_DOWNLOAD_PATTERN);
        for (String line : Files.readAllLines(patterns)) {
            addLine(line);
        }
    }

    public FileBasedSyncBlacklist(InputStream patternStream) throws IOException {
        add(TMP_DOWNLOAD_PATTERN);
        BufferedReader reader = new BufferedReader(new InputStreamReader(patternStream));
        String line;
        while ((line = reader.readLine()) != null) {
            addLine(line);
        }
    }

    private void addLine(String line) {
        try {
            line = line.trim();
            if (line.isEmpty()) {
                return;
            }

            add(Pattern.compile(line));
        } catch (PatternSyntaxException e) {
            logger.warn("failed to load blacklist pattern: '" + line + "' (" + e.getMessage() + ")", e);
        }
    }
}
