package de.qabel.desktop.daemon.sync.blacklist;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
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
			try {
				line = line.trim();
				if (line.isEmpty()) {
					continue;
				}

				add(Pattern.compile(line));
			} catch (PatternSyntaxException e) {
				logger.warn("failed to load blacklist pattern: '" + line + "' (" + e.getMessage() + ")", e);
			}
		}
	}
}
