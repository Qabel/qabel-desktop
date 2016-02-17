package de.qabel.desktop.daemon.drop;

import org.junit.Test;

import static org.junit.Assert.*;

public class ShareNotificationMessageTest {
	@Test
	public void formatsToAndFromItself() {
		ShareNotificationMessage message = new ShareNotificationMessage("http://somewhere", "key", "message");
		String json = message.toJson();

		ShareNotificationMessage unserializedMessage = ShareNotificationMessage.fromJson(json);

		assertEquals("http://somewhere", unserializedMessage.getUrl());
		assertEquals("key", unserializedMessage.getHexKey());
		assertEquals("message", unserializedMessage.getMessage());
	}
}
