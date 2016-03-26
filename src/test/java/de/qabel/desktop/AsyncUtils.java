package de.qabel.desktop;

import java.util.concurrent.Callable;

import static org.junit.Assert.fail;

public class AsyncUtils {
	public static void waitUntil(Callable<Boolean> evaluate) {
		waitUntil(evaluate, 2000L);
	}

	public static void waitUntil(Callable<Boolean> evaluate, long timeout) {
		waitUntil(evaluate, timeout, () -> "wait timeout");
	}

	public static void waitUntil(Callable<Boolean> evaluate, Callable<String> errorMessage) {
		waitUntil(evaluate, 2000L, () -> "wait timeout");
	}

	public static void waitUntil(Callable<Boolean> evaluate, long timeout, Callable<String> errorMessage) {
		long startTime = System.currentTimeMillis();
		try {
			while (!evaluate.call()) {
				Thread.yield();
				Thread.sleep(10);
				if (System.currentTimeMillis() - timeout > startTime) {
					fail(errorMessage.call());
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			fail(e.getMessage());
		}
	}
}
