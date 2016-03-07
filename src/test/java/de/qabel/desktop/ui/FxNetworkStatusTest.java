package de.qabel.desktop.ui;

import de.qabel.desktop.daemon.NetworkStatus;
import javafx.application.Platform;
import org.junit.Test;

import java.util.LinkedList;
import java.util.List;

import static org.junit.Assert.*;

public class FxNetworkStatusTest extends AbstractControllerTest {
	@Test
	public void statusIsChangedInPlatformThread() {
		NetworkStatus status = new NetworkStatus();
		FxNetworkStatus fxStatus = new FxNetworkStatus(status);

		final List<Object> updates = new LinkedList<>();
		fxStatus.onlineProperty().addListener((instance, oldValue, newValue) -> {
			if (!Platform.isFxApplicationThread()) {
				fail("update did not happen inside the application thread");
			}
			updates.add(newValue);
		});

		status.offline();
		waitUntil(() -> !updates.isEmpty());
		status.online();
		waitUntil(() -> updates.size() == 2);
	}
}