package de.qabel.desktop.ui.connector;

import de.qabel.core.http.DropHTTP;
import de.qabel.core.http.HTTPResult;

import java.net.URI;
import java.util.Collection;

public class StubDropHttp extends DropHTTP {
	public HTTPResult<Collection<byte[]>> messages = new HTTPResult<>();

	@Override
	public HTTPResult<Collection<byte[]>> receiveMessages(URI uri, long sinceDate) {
		return messages;
	}
}
