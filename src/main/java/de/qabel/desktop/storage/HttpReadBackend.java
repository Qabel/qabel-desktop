package de.qabel.desktop.storage;

import de.qabel.desktop.exceptions.QblStorageException;
import de.qabel.desktop.exceptions.QblStorageNotFound;
import org.apache.http.HttpEntity;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpRequest;
import org.apache.http.HttpStatus;
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

public class HttpReadBackend extends AbstractHttpStorageBackend implements StorageReadBackend {

	public HttpReadBackend(String root) {
		super(root);
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
			uri = new URI(this.root).resolve(name);
		} catch (URISyntaxException e) {
			throw new QblStorageException(e);
		}
		HttpGet httpGet = new HttpGet(uri);
		if (ifModifiedVersion != null) {
			httpGet.addHeader(HttpHeaders.IF_NONE_MATCH, ifModifiedVersion);
		}
		prepareRequest(httpGet);

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
