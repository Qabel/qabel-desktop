package de.qabel.desktop.storage;

import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.DefaultHttpRequestRetryHandler;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;

public class AbstractHttpBackend {
	// Number of http connections to S3
	// The default was too low, 20 works. Further testing may be required
	// to find the best amount of connections.
	protected static final int CONNECTIONS = 50;
	protected final CloseableHttpClient httpclient;
	String root;

	public AbstractHttpBackend(String root) {
		if (!root.endsWith("/")) {
			root += "/";
		}
		this.root = root;
		// Increase max total connection
		PoolingHttpClientConnectionManager connManager = new PoolingHttpClientConnectionManager();
		connManager.setMaxTotal(CONNECTIONS);
		// Increase default max connection per route
		// Set to the max total because we only have 1 route
		connManager.setDefaultMaxPerRoute(CONNECTIONS);

		httpclient = HttpClients.custom()
				.setRetryHandler(new DefaultHttpRequestRetryHandler(0, false))
				.setConnectionManager(connManager).build();
	}
}
