package de.qabel.desktop.storage;

import de.qabel.desktop.exceptions.QblStorageException;
import de.qabel.desktop.exceptions.QblStorageNotFound;
import org.apache.http.HttpEntity;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.utils.DateUtils;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class S3ReadBackend implements StorageReadBackend {
	private static final SimpleDateFormat lastModifiedFormat = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss zzz");
	private static final Logger logger = LoggerFactory.getLogger(S3ReadBackend.class.getSimpleName());

	// Number of http connections to S3
	// The default was too low, 20 works. Further testing may be required
	// to find the best amount of connections.
	private static final int CONNECTIONS = 50;

	String root;
	private final CloseableHttpClient httpclient;

	S3ReadBackend(String bucket, String prefix) {
		this("https://" + bucket + ".s3.amazonaws.com/" + prefix);
	}

	S3ReadBackend(String root) {
		this.root = root;
		// Increase max total connection
		PoolingHttpClientConnectionManager connManager = new PoolingHttpClientConnectionManager();
		connManager.setMaxTotal(CONNECTIONS);
		// Increase default max connection per route
		// Set to the max total because we only have 1 route
		connManager.setDefaultMaxPerRoute(CONNECTIONS);

		httpclient = HttpClients.custom()
				.setConnectionManager(connManager).build();

		logger.info("S3ReadBackend with root address set to " + root);
	}

	public StorageDownload download(String name) throws QblStorageException {
		try {
			return download(name, null);
		} catch (UnmodifiedException e) {
			throw new IllegalStateException(e);
		}
	}

	public StorageDownload download(String name, String ifModifiedVersion) throws QblStorageException, UnmodifiedException {
		logger.info("Downloading " + name);
		URI uri;
		try {
			uri = new URI(this.root + '/' + name);
		} catch (URISyntaxException e) {
			throw new QblStorageException(e);
		}
		HttpGet httpGet = new HttpGet(uri);
		if (ifModifiedVersion != null) {
			httpGet.addHeader(HttpHeaders.IF_NONE_MATCH, ifModifiedVersion);
		}
		CloseableHttpResponse response;
		try {
			response = httpclient.execute(httpGet);
		} catch (IOException e) {
			throw new QblStorageException(e);
		}
		int status = response.getStatusLine().getStatusCode();
		if ((status == 404) || (status == 403)) {
			throw new QblStorageNotFound("File not found");
		}
		if (status == HttpStatus.SC_NOT_MODIFIED) {
			throw new UnmodifiedException();
		}
		if (status != 200) {
			throw new QblStorageException("Download error");
		}
		String modifiedVersion = response.getFirstHeader(HttpHeaders.ETAG).getValue();

		if (ifModifiedVersion != null && modifiedVersion.equals(ifModifiedVersion)) {
			throw new UnmodifiedException();
		}
		HttpEntity entity = response.getEntity();
		if (entity == null) {
			throw new QblStorageException("No content");
		}
		try {
			InputStream content = entity.getContent();
			return new StorageDownload(content, modifiedVersion, entity.getContentLength());
		} catch (IOException e) {
			throw new QblStorageException(e);
		}
	}
}
