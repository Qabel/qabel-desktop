package de.qabel.desktop.daemon.sync;

import java.util.concurrent.Callable;

import static org.junit.Assert.fail;

public class SyncUtils {
	public static void waitUntil(Callable<Boolean> evaluate) {
		waitUntil(evaluate, null);
	}

	public static void waitUntil(Callable<Boolean> evaluate, Callable<String> errorMessage) {
		waitUntil(evaluate, 1000L, errorMessage);
	}

	public static void waitUntil(Callable<Boolean> evaluate, long timeout) {
		waitUntil(evaluate, timeout, null);
	}

	public static void waitUntil(Callable<Boolean> evaluate, long timeout, Callable<String> errorMessage) {
		try {
			long startTime = System.currentTimeMillis();
			while (!evaluate.call()) {
				Thread.yield();
				if (System.currentTimeMillis() - timeout > startTime) {
					fail(errorMessage != null ? errorMessage.call() : "wait timeout");
				}
			}
		} catch (Exception e) {
			fail(e.getMessage());
		}
	}
}
