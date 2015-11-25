package de.qabel.desktop.storage;

import de.qabel.desktop.exceptions.QblStorageException;
import de.qabel.desktop.exceptions.QblStorageNotFound;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;

class S3ReadBackend extends StorageReadBackend {

	private static final Logger logger = LoggerFactory.getLogger(S3ReadBackend.class.getName());

	String root;
	private final CloseableHttpClient httpclient;

	S3ReadBackend(String bucket, String prefix) {
		 this("https://"+bucket+".s3.amazonaws.com/"+prefix);
	}

	S3ReadBackend(String root) {
		this.root = root;
		// Increase max total connection
		PoolingHttpClientConnectionManager connManager = new PoolingHttpClientConnectionManager();
		connManager.setMaxTotal(20);
		// Increase default max connection per route
		connManager.setDefaultMaxPerRoute(20);

		httpclient = HttpClients.custom()
				.setConnectionManager(connManager).build();

		logger.info("S3ReadBackend with root address set to " + root);
	}

	InputStream download(String name) throws QblStorageException {
		logger.info("Downloading " + name);
		URI uri;
		try {
			uri = new URI(this.root + '/' + name);
		} catch (URISyntaxException e) {
			throw new QblStorageException(e);
		}
		HttpGet httpGet = new HttpGet(uri);
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
		if (status != 200) {
			throw new QblStorageException("Download error");
		}
		HttpEntity entity = response.getEntity();
		if (entity == null) {
			throw new QblStorageException("No content");
		}
		try {
			InputStream content = entity.getContent();
			return content;
		} catch (IOException e) {
			throw new QblStorageException(e);
		}
	}
}
